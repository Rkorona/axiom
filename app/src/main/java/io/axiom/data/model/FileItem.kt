package io.axiom.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * 代表文件系统中的单个文件或目录条目。
 * 标注 @Immutable 确保 Compose 在 List 重绘时可以安全跳过未修改的 Card。
 */
@Immutable
data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val extension: String,
    val lastModified: Long = System.currentTimeMillis(),
    val size: Long = 0L,
    val isPinned: Boolean = false,
    val language: CodeLanguage = CodeLanguage.UNKNOWN,
    val isDirectory: Boolean = false,
    val parentPath: String = "",
    val depth: Int = 0
)

enum class CommandMode {
    FILE,
    COMMAND,
    SYMBOL
}

enum class CodeLanguage(
    val displayName: String,
    val extensions: List<String>,
    val colorHex: Long
) {
    KOTLIN("Kotlin", listOf("kt", "kts"), 0xFF7F52FF),
    JAVA("Java", listOf("java"), 0xFFED8B00),
    PYTHON("Python", listOf("py", "pyw"), 0xFF3776AB),
    JAVASCRIPT("JavaScript", listOf("js", "mjs", "cjs"), 0xFFF7DF1E),
    TYPESCRIPT("TypeScript", listOf("ts", "tsx"), 0xFF3178C6),
    RUST("Rust", listOf("rs"), 0xFFCE422B),
    CPP("C++", listOf("cpp", "cxx", "cc", "hpp"), 0xFF00599C),
    C("C", listOf("c", "h"), 0xFFA8B9CC),
    GO("Go", listOf("go"), 0xFF00ADD8),
    SWIFT("Swift", listOf("swift"), 0xFFFA7343),
    DART("Dart", listOf("dart"), 0xFF00B4AB),
    HTML("HTML", listOf("html", "htm"), 0xFFE34F26),
    CSS("CSS", listOf("css", "scss", "sass", "less"), 0xFF1572B6),
    JSON("JSON", listOf("json"), 0xFF92C544),
    XML("XML", listOf("xml"), 0xFFFF6600),
    MARKDOWN("Markdown", listOf("md", "mdx"), 0xFF083FA1),
    YAML("YAML", listOf("yml", "yaml"), 0xFFCB171E),
    SHELL("Shell", listOf("sh", "bash", "zsh"), 0xFF4EAA25),
    UNKNOWN("Text", emptyList(), 0xFF8B8FA8)
}

@Immutable
data class AppCommand(
    val id: String,
    val title: String,
    val description: String,
    val shortcut: String? = null,
    val category: CommandCategory = CommandCategory.GENERAL
)

enum class CommandCategory(val label: String) {
    GENERAL("General"),
    FILE("File"),
    EDIT("Edit"),
    VIEW("View"),
    GIT("Git"),
    TERMINAL("Terminal")
}

@Stable
sealed class SearchResult {
    @Immutable
    data class FileResult(val file: FileItem) : SearchResult()
    @Immutable
    data class CommandResult(val command: AppCommand) : SearchResult()
    @Immutable
    data class SymbolResult(
        val symbol: String,
        val kind: SymbolKind,
        val file: FileItem,
        val line: Int
    ) : SearchResult()
}

enum class SymbolKind(val label: String) {
    FUNCTION("fun"),
    CLASS("class"),
    INTERFACE("interface"),
    VARIABLE("val"),
    MUTABLE_VARIABLE("var"),
    OBJECT("object"),
    ENUM("enum"),
    ANNOTATION("annotation")
}

@Immutable
data class GroupedResults(
    val files: List<SearchResult.FileResult> = emptyList(),
    val commands: List<SearchResult.CommandResult> = emptyList(),
    val symbols: List<SearchResult.SymbolResult> = emptyList()
) {
    val isEmpty: Boolean get() = files.isEmpty() && commands.isEmpty() && symbols.isEmpty()
    val totalCount: Int get() = files.size + commands.size + symbols.size
}

fun List<SearchResult>.toGrouped(): GroupedResults = GroupedResults(
    files = filterIsInstance<SearchResult.FileResult>(),
    commands = filterIsInstance<SearchResult.CommandResult>(),
    symbols = filterIsInstance<SearchResult.SymbolResult>()
)

fun String.toCodeLanguage(): CodeLanguage {
    val ext = this.lowercase().trimStart('.')
    return CodeLanguage.entries.firstOrNull { ext in it.extensions } ?: CodeLanguage.UNKNOWN
}