package io.axiom.ui.editor

import io.axiom.data.model.CommandMode
import io.axiom.data.model.EditorSettings
import io.axiom.data.model.FileItem
import io.axiom.data.model.GroupedResults
import io.axiom.data.model.Project

/**
 * Immutable snapshot of everything [EditorScreen] needs to render.
 *
 * Mirrors the shape of [HomeUiState] for the command bar fields so the same
 * [CommandBar] component can be reused without modification.
 */
data class EditorUiState(
    /** The open project, null while loading. */
    val project: Project? = null,

    // ── File tree ────────────────────────────────────────────────────────────
    /** All files discovered in the project root. */
    val files: List<FileItem> = emptyList(),
    /** True while the initial file scan is in progress. */
    val isLoadingFiles: Boolean = false,

    // ── Open file / editor surface ───────────────────────────────────────────
    /** The file currently displayed in the editor, or null for the empty state. */
    val openFile: FileItem? = null,
    /** Raw text content of [openFile]. */
    val fileContent: String = "",
    /** True while file bytes are being read from disk. */
    val isLoadingContent: Boolean = false,
    /** True when [fileContent] differs from the last saved version. */
    val isDirty: Boolean = false,

    // ── Command bar ──────────────────────────────────────────────────────────
    val query: String = "",
    val commandMode: CommandMode = CommandMode.FILE,
    val isCommandBarFocused: Boolean = false,
    val isSearching: Boolean = false,
    val groupedResults: GroupedResults = GroupedResults(),
    val showEmptyState: Boolean = false,
    val placeholderIndex: Int = 0,

    // ── App settings (mirrored from AppSettingsRepository) ───────────────────
    val editorSettings:     EditorSettings = EditorSettings(),
    val animatedBackground: Boolean        = true,
    val accentKey:          String         = "violet"
)

/** Placeholder hints that cycle in the editor's Command Bar when idle. */
val editorCommandBarHints: List<String> = listOf(
    "Search in project…",
    "Type > for commands",
    "Type # to jump to symbol",
    "Navigate anything, instantly"
)
