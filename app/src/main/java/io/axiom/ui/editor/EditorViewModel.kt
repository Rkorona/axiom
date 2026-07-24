package io.axiom.ui.editor

import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.axiom.data.model.AppCommand
import io.axiom.data.model.CodeLanguage
import io.axiom.data.model.CommandCategory
import io.axiom.data.model.CommandMode
import io.axiom.data.model.FileItem
import io.axiom.data.model.GroupedResults
import io.axiom.data.model.SearchResult
import io.axiom.data.model.toCodeLanguage
import io.axiom.data.model.toGrouped
import io.axiom.data.repository.ProjectRepository
import io.axiom.data.util.FileSystemUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import io.axiom.data.model.EditorSettings
import io.axiom.data.repository.AppSettingsRepository

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var hintCycleJob: Job? = null

    init {
        // Mirror AppSettingsRepository into uiState so the editor reacts live.
        viewModelScope.launch {
            combine(
                AppSettingsRepository.fontSize,
                AppSettingsRepository.wordWrap,
                AppSettingsRepository.lineNumbers
            ) { fontSize, wordWrap, lineNumbers ->
                EditorSettings(
                    fontSize    = fontSize,
                    wordWrap    = wordWrap,
                    lineNumbers = lineNumbers
                )
            }
            .collect { s -> _uiState.update { it.copy(editorSettings = s) } }
        }

        viewModelScope.launch {
            combine(
                AppSettingsRepository.animatedBackground,
                AppSettingsRepository.accentKey
            ) { animated, accent -> Pair(animated, accent) }
            .collect { (animated, accent) ->
                _uiState.update { it.copy(animatedBackground = animated, accentKey = accent) }
            }
        }
    }

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            val project = repository.getProjectById(projectId) ?: return@launch
            _uiState.update { it.copy(project = project, isLoadingFiles = true) }

            val files = withContext(Dispatchers.IO) {
                if (project.isExternal) scanSafFiles(project.rootUri)
                else FileSystemUtils.scanFiles(project.rootUri)
            }

            _uiState.update { it.copy(files = files, isLoadingFiles = false) }
            startHintCycle()
        }
    }

    fun onFileClick(file: FileItem) {
        val project = _uiState.value.project ?: return
        _uiState.update { it.copy(openFile = file, isLoadingContent = true, fileContent = "") }

        viewModelScope.launch {
            val content = withContext(Dispatchers.IO) {
                try {
                    if (project.isExternal) {
                        getApplication<Application>().contentResolver
                            .openInputStream(Uri.parse(file.path))
                            ?.bufferedReader()?.use { it.readText() }
                            ?: "// Cannot read file via SAF storage"
                    } else {
                        val fullPath = buildPath(project.rootUri, file)
                        File(fullPath).readText()
                    }
                } catch (e: Exception) {
                    "// Error reading file: ${e.localizedMessage}"
                }
            }
            _uiState.update {
                it.copy(fileContent = content, isLoadingContent = false, isDirty = false)
            }
            repository.updateLastEditedFile(project, file.name)
        }
    }

    fun onContentChange(text: String) {
        _uiState.update { it.copy(fileContent = text, isDirty = true) }
    }

    fun saveCurrentFile() {
        val state   = _uiState.value
        val project = state.project ?: return
        val file    = state.openFile ?: return
        if (project.isExternal) return

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                File(buildPath(project.rootUri, file)).writeText(state.fileContent)
                _uiState.update { it.copy(isDirty = false) }
            }
        }
    }

    fun onQueryChange(query: String) {
        val mode = when {
            query.startsWith(">") -> CommandMode.COMMAND
            query.startsWith("#") -> CommandMode.SYMBOL
            else                  -> CommandMode.FILE
        }
        _uiState.update { it.copy(query = query, commandMode = mode, isSearching = true) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            val results = withContext(Dispatchers.Default) { performSearch(query, mode) }
            val grouped = results.toGrouped()
            _uiState.update {
                it.copy(
                    groupedResults = grouped,
                    isSearching    = false,
                    showEmptyState = query.isNotBlank() && grouped.isEmpty
                )
            }
        }
    }

    /** Called when the user taps the code editor surface (keyboard about to open). */
    fun onEditorFocused() {
        _uiState.update { it.copy(bottomBarMode = BottomBarMode.SYMBOLS) }
    }

    /** Called when the bottom bar should switch to a specific mode. */
    fun onBottomBarModeChange(mode: BottomBarMode) {
        _uiState.update { it.copy(bottomBarMode = mode) }
    }

    fun onCommandBarFocusChange(focused: Boolean) {
        _uiState.update { it.copy(isCommandBarFocused = focused) }
        if (focused) {
            hintCycleJob?.cancel()
            // Always show the command bar while the user is actively searching.
            _uiState.update { it.copy(bottomBarMode = BottomBarMode.COMMAND) }
        } else {
            startHintCycle()
        }
    }

    fun onClearQuery() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                query          = "",
                commandMode    = CommandMode.FILE,
                groupedResults = GroupedResults(),
                isSearching    = false,
                showEmptyState = false
            )
        }
    }

    fun onCommandClick(command: AppCommand) {
        when (command.id) {
            "save" -> saveCurrentFile()
        }
    }

    private fun startHintCycle() {
        hintCycleJob?.cancel()
        hintCycleJob = viewModelScope.launch {
            var index = 0
            while (true) {
                delay(3_000L)
                index = (index + 1) % editorCommandBarHints.size
                _uiState.update { it.copy(placeholderIndex = index) }
            }
        }
    }

    private fun performSearch(query: String, mode: CommandMode): List<SearchResult> {
        val q = when (mode) {
            CommandMode.COMMAND -> query.removePrefix(">").trim()
            CommandMode.SYMBOL  -> query.removePrefix("#").trim()
            CommandMode.FILE    -> query.trim()
        }
        if (q.isBlank()) return emptyList()

        return when (mode) {
            CommandMode.FILE -> _uiState.value.files
                .asSequence()
                .filter { !it.isDirectory && it.name.contains(q, ignoreCase = true) }
                .take(12)
                .map { SearchResult.FileResult(it) }
                .toList()

            CommandMode.COMMAND -> EDITOR_COMMANDS
                .asSequence()
                .filter { it.title.contains(q, ignoreCase = true) || it.description.contains(q, ignoreCase = true) }
                .take(8)
                .map { SearchResult.CommandResult(it) }
                .toList()

            CommandMode.SYMBOL -> emptyList()
        }
    }

    private fun buildPath(rootUri: String, file: FileItem): String =
        if (file.path.isEmpty()) "$rootUri/${file.name}"
        else "$rootUri/${file.path}${file.name}"

    private suspend fun scanSafFiles(rootUriString: String): List<FileItem> = withContext(Dispatchers.IO) {
        val app     = getApplication<Application>()
        val treeUri = Uri.parse(rootUriString)
        return@withContext try {
            val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
            scanSafDirectory(app, treeUri, rootDocId, parentPath = "", depth = 0)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun scanSafDirectory(
        app: Application,
        treeUri: Uri,
        dirDocId: String,
        parentPath: String,
        depth: Int,
        maxDepth: Int = 4
    ): List<FileItem> {
        if (depth > maxDepth) return emptyList()

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, dirDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )
        val cursor = app.contentResolver.query(childrenUri, projection, null, null, null)
            ?: return emptyList()

        data class Entry(val docId: String, val item: FileItem)
        val entries = mutableListOf<Entry>()

        cursor.use { c ->
            while (c.moveToNext()) {
                val docId = c.getString(0) ?: continue
                val name  = c.getString(1) ?: continue
                val mime  = c.getString(2) ?: ""
                val size  = if (c.isNull(3)) 0L else c.getLong(3)
                val mod   = if (c.isNull(4)) 0L else c.getLong(4)

                if (name.startsWith(".")) continue

                val isDir  = mime == DocumentsContract.Document.MIME_TYPE_DIR
                val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                val ext    = if (isDir) "" else name.substringAfterLast('.', "").lowercase()

                entries.add(
                    Entry(
                        docId = docId,
                        item  = FileItem(
                            id           = docId,
                            name         = name,
                            path         = docUri.toString(),
                            extension    = ext,
                            lastModified = mod,
                            size         = if (isDir) 0L else size,
                            language     = if (isDir) CodeLanguage.UNKNOWN else ext.toCodeLanguage(),
                            isDirectory  = isDir,
                            parentPath   = parentPath,
                            depth        = depth
                        )
                    )
                )
            }
        }

        entries.sortWith(compareBy({ !it.item.isDirectory }, { it.item.name }))

        val result = mutableListOf<FileItem>()
        for (entry in entries) {
            result.add(entry.item)
            if (entry.item.isDirectory) {
                result.addAll(
                    scanSafDirectory(app, treeUri, entry.docId, entry.item.path, depth + 1, maxDepth)
                )
            }
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        hintCycleJob?.cancel()
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 100L

        private val EDITOR_COMMANDS = listOf(
            AppCommand("save",           "Save File",        "Save the currently open file",                null, CommandCategory.FILE),
            AppCommand("save_all",       "Save All",         "Save all unsaved changes",                    null, CommandCategory.FILE),
            AppCommand("new_file",       "New File",         "Create a new file in this project",           null, CommandCategory.FILE),
            AppCommand("new_folder",     "New Folder",       "Create a new folder in this project",         null, CommandCategory.FILE),
            AppCommand("close_file",     "Close File",       "Close the active file",                       null, CommandCategory.FILE),
            AppCommand("find_replace",   "Find & Replace",   "Search and replace in current file",          null, CommandCategory.EDIT),
            AppCommand("format_doc",     "Format Document",  "Run code formatter on current file",          null, CommandCategory.EDIT),
            AppCommand("rename_symbol",  "Rename Symbol",    "Rename symbol under cursor",                  null, CommandCategory.EDIT),
            AppCommand("open_terminal",  "Open Terminal",    "Open integrated terminal panel",              null, CommandCategory.TERMINAL),
            AppCommand("split_editor",   "Split Editor",     "Split editor horizontally or vertically",     null, CommandCategory.VIEW),
            AppCommand("toggle_minimap", "Toggle Minimap",   "Show or hide the code minimap",               null, CommandCategory.VIEW),
            AppCommand("toggle_theme",   "Toggle Theme",     "Switch between dark and light colour scheme", null, CommandCategory.VIEW),
            AppCommand("git_status",     "Git Status",       "Show working tree status",                    null, CommandCategory.GIT),
            AppCommand("git_commit",     "Git Commit",       "Stage changes and commit",                    null, CommandCategory.GIT),
            AppCommand("git_push",       "Git Push",         "Push commits to remote",                      null, CommandCategory.GIT),
            AppCommand("settings",       "Settings",         "Open application settings",                   null, CommandCategory.GENERAL),
            AppCommand("command_palette","Command Palette",  "Open this command palette",                   null, CommandCategory.GENERAL),
        )
    }
}