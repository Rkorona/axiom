package io.axiom.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.axiom.data.db.AxiomDatabase
import io.axiom.data.db.ProjectEntity
import io.axiom.data.model.CodeLanguage
import io.axiom.data.model.Project
import io.axiom.data.util.FileSystemUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Single source of truth for [Project] data.
 *
 * Storage strategy:
 * - New projects   → getExternalFilesDir("projects")/<name>/  (no permission needed)
 * - Imported folders → SAF Uri with persisted permission via takePersistableUriPermission()
 */
class ProjectRepository(private val context: Context) {

    private val dao = AxiomDatabase.getInstance(context).projectDao()

    /** Reactive stream of all projects, pinned first then by recency. */
    val allProjects: Flow<List<Project>> =
        dao.observeAll().map { it.map(ProjectEntity::toProject) }

    /** Reactive stream of the [limit] most recent projects. */
    fun recentProjects(limit: Int = 6): Flow<List<Project>> =
        dao.observeRecent(limit).map { it.map(ProjectEntity::toProject) }

    /**
     * Create a new project in the app's own external files directory.
     * No special storage permission is required (minSdk 36).
     *
     * @param name     Human-readable project name.
     * @param language Primary language (optional; auto-detected after creation if UNKNOWN).
     * @return The persisted [Project], or null if directory creation failed.
     */
    suspend fun createProject(
        name: String,
        language: CodeLanguage = CodeLanguage.UNKNOWN
    ): Project? = withContext(Dispatchers.IO) {
        val baseDir = context.getExternalFilesDir("projects")
            ?: context.filesDir.resolve("projects").also { it.mkdirs() }
        baseDir.mkdirs()

        val projectDir = FileSystemUtils.createProjectDir(baseDir, name) ?: return@withContext null

        // If language not explicitly set, try auto-detection (will likely be UNKNOWN for empty dir)
        val detectedLanguage = if (language == CodeLanguage.UNKNOWN) {
            FileSystemUtils.detectLanguage(projectDir.absolutePath)
        } else {
            language
        }

        val project = Project(
            name       = name,
            rootUri    = projectDir.absolutePath,
            isExternal = false,
            language   = detectedLanguage,
            lastOpened = System.currentTimeMillis()
        )
        val id = dao.insert(ProjectEntity.fromProject(project))
        project.copy(id = id)
    }

    /**
     * Import an existing folder via a SAF document tree URI.
     * Takes a persistent read+write permission so the access survives restarts.
     *
     * @param name      Display name for the project (typically the folder name).
     * @param uriString The URI string returned from ACTION_OPEN_DOCUMENT_TREE.
     */
    suspend fun importProject(name: String, uriString: String): Project =
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(uriString)
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            val project = Project(
                name       = name.ifBlank { "Imported Project" },
                rootUri    = uriString,
                isExternal = true,
                language   = CodeLanguage.UNKNOWN,  // SAF tree walking is expensive; detect later
                lastOpened = System.currentTimeMillis()
            )
            val id = dao.insert(ProjectEntity.fromProject(project))
            project.copy(id = id)
        }

    /** Delete a project record from the database (does NOT delete files). */
    suspend fun deleteProject(project: Project) = dao.deleteById(project.id)

    /** Toggle the pinned state of a project. */
    suspend fun togglePin(project: Project) =
        dao.updatePinned(project.id, !project.isPinned)

    /** Stamp the project as opened right now. */
    suspend fun touchLastOpened(project: Project) =
        dao.updateLastOpened(project.id, System.currentTimeMillis())

    /** Record the most recently edited file path for the project's card subtitle. */
    suspend fun updateLastEditedFile(project: Project, filePath: String) =
        dao.updateLastEditedFile(project.id, filePath)
}
