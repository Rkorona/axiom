package io.axiom.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.axiom.data.model.CodeLanguage
import io.axiom.data.model.Project

/**
 * Room entity that mirrors [Project].
 * [CodeLanguage] is stored as its enum name string for future-proofing.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rootUri: String,
    val isExternal: Boolean,
    val language: String,          // CodeLanguage.name
    val lastOpened: Long,
    val isPinned: Boolean,
    val gitRemote: String?,
    val lastEditedFile: String?
) {
    fun toProject(): Project = Project(
        id            = id,
        name          = name,
        rootUri       = rootUri,
        isExternal    = isExternal,
        language      = CodeLanguage.entries.firstOrNull { it.name == language }
                        ?: CodeLanguage.UNKNOWN,
        lastOpened    = lastOpened,
        isPinned      = isPinned,
        gitRemote     = gitRemote,
        lastEditedFile = lastEditedFile
    )

    companion object {
        fun fromProject(project: Project): ProjectEntity = ProjectEntity(
            id             = project.id,
            name           = project.name,
            rootUri        = project.rootUri,
            isExternal     = project.isExternal,
            language       = project.language.name,
            lastOpened     = project.lastOpened,
            isPinned       = project.isPinned,
            gitRemote      = project.gitRemote,
            lastEditedFile = project.lastEditedFile
        )
    }
}
