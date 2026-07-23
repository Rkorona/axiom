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
 * [Project] 数据唯一真实来源 (Single Source of Truth)。
 */
class ProjectRepository(private val context: Context) {

    private val dao = AxiomDatabase.getInstance(context).projectDao()

    val allProjects: Flow<List<Project>> =
        dao.observeAll().map { list -> list.map(ProjectEntity::toProject) }

    fun recentProjects(limit: Int = 6): Flow<List<Project>> =
        dao.observeRecent(limit).map { list -> list.map(ProjectEntity::toProject) }

    suspend fun createProject(
        name: String,
        language: CodeLanguage = CodeLanguage.UNKNOWN
    ): Project? = withContext(Dispatchers.IO) {
        val baseDir = context.getExternalFilesDir("projects")
            ?: context.filesDir.resolve("projects").also { it.mkdirs() }
        baseDir.mkdirs()

        val projectDir = FileSystemUtils.createProjectDir(baseDir, name) ?: return@withContext null

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
     * 导入 SAF 文件夹，并安全持有 Persistable URI Permission。
     */
    suspend fun importProject(name: String, uriString: String): Project =
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(uriString)
            
            // 🛡️ 边界防错：捕获 SAF 权限申请时可能抛出的 SecurityException
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
                language   = CodeLanguage.UNKNOWN,
                lastOpened = System.currentTimeMillis()
            )
            val id = dao.insert(ProjectEntity.fromProject(project))
            project.copy(id = id)
        }

    suspend fun deleteProject(project: Project) = dao.deleteById(project.id)

    suspend fun togglePin(project: Project) =
        dao.updatePinned(project.id, !project.isPinned)

    suspend fun touchLastOpened(project: Project) =
        dao.updateLastOpened(project.id, System.currentTimeMillis())

    suspend fun updateLastEditedFile(project: Project, filePath: String) =
        dao.updateLastEditedFile(project.id, filePath)

    suspend fun getProjectById(id: Long): Project? =
        withContext(Dispatchers.IO) { dao.getById(id)?.toProject() }
}