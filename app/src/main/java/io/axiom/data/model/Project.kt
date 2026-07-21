package io.axiom.data.model

/**
 * Domain model for a project managed by Axiom.
 *
 * Storage strategy (Hybrid):
 *  - [isExternal] = false → [rootUri] is an absolute path under getExternalFilesDir("projects")
 *  - [isExternal] = true  → [rootUri] is a SAF-persisted Uri string (ACTION_OPEN_DOCUMENT_TREE)
 */
data class Project(
    val id: Long = 0,
    val name: String,

    /**
     * For internal projects: absolute path string (e.g. "/sdcard/Android/data/io.axiom/files/projects/MyApp").
     * For external projects: SAF Uri string (e.g. "content://com.android.externalstorage…").
     */
    val rootUri: String,

    /** True if this project was imported via SAF; false if created in app's own dir. */
    val isExternal: Boolean = false,

    /** Auto-detected primary language of the project. */
    val language: CodeLanguage = CodeLanguage.UNKNOWN,

    /** Unix timestamp (ms) of the last time this project was opened. */
    val lastOpened: Long = System.currentTimeMillis(),

    /** Whether the user has pinned this project to the top of the list. */
    val isPinned: Boolean = false,

    /** Optional git remote URL — reserved for future use. */
    val gitRemote: String? = null,

    /** Display hint: name of the most recently edited file in this project. */
    val lastEditedFile: String? = null
)
