package io.axiom.data.util

import io.axiom.data.model.CodeLanguage
import io.axiom.data.model.FileItem
import io.axiom.data.model.toCodeLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 高性能文件系统工具类。
 */
object FileSystemUtils {

    /**
     * 自动检测项目主语言。
     */
    suspend fun detectLanguage(rootPath: String): CodeLanguage = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) return@withContext CodeLanguage.UNKNOWN

        val indicators = linkedMapOf(
            "build.gradle.kts" to CodeLanguage.KOTLIN,
            "build.gradle"     to CodeLanguage.KOTLIN,
            "tsconfig.json"    to CodeLanguage.TYPESCRIPT,
            "package.json"     to CodeLanguage.JAVASCRIPT,
            "requirements.txt" to CodeLanguage.PYTHON,
            "pyproject.toml"   to CodeLanguage.PYTHON,
            "Cargo.toml"       to CodeLanguage.RUST,
            "go.mod"           to CodeLanguage.GO,
            "pubspec.yaml"     to CodeLanguage.DART,
            "Package.swift"    to CodeLanguage.SWIFT,
            "pom.xml"          to CodeLanguage.JAVA
        )

        root.listFiles()?.forEach { file ->
            indicators[file.name]?.let { return@withContext it }
        }

        val extFreq = mutableMapOf<String, Int>()
        root.walkTopDown()
            .maxDepth(3)
            .asSequence()
            .filter { it.isFile }
            .forEach { file ->
                val ext = file.extension.lowercase()
                if (ext.isNotBlank()) {
                    extFreq[ext] = (extFreq[ext] ?: 0) + 1
                }
            }

        val topExt = extFreq.maxByOrNull { it.value }?.key ?: return@withContext CodeLanguage.UNKNOWN
        return@withContext CodeLanguage.entries.firstOrNull { topExt in it.extensions }
            ?: CodeLanguage.UNKNOWN
    }

    /**
     * 创建项目根目录。
     */
    suspend fun createProjectDir(baseDir: File, projectName: String): File? = withContext(Dispatchers.IO) {
        val sanitized = projectName.trim()
            .replace(Regex("[^a-zA-Z0-9_.\\- ]"), "_")
            .replace(' ', '_')
            .ifBlank { "project_${System.currentTimeMillis()}" }
        val projectDir = File(baseDir, sanitized)
        if (projectDir.mkdirs() || projectDir.exists()) projectDir else null
    }

    /**
     * 惰性递归扫描本地项目目录，采用 Sequence 规避大工程内存暴涨。
     */
    suspend fun scanFiles(rootPath: String, maxFiles: Int = 400): List<FileItem> = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) return@withContext emptyList()

        val skipDirs = setOf(
            "node_modules", "build", ".gradle", ".git",
            "__pycache__", "target", ".idea", ".dart_tool", ".pub-cache"
        )

        root.walkTopDown()
            .onEnter { dir -> !dir.name.startsWith(".") && dir.name !in skipDirs }
            .asSequence()
            .filter { it.isFile && !it.name.startsWith(".") }
            .take(maxFiles)
            .mapIndexed { index, file ->
                val relativePath = file.relativeTo(root).parentFile?.path ?: ""
                val ext = file.extension.lowercase()
                FileItem(
                    id           = index.toString(),
                    name         = file.name,
                    path         = if (relativePath.isEmpty()) "" else "$relativePath/",
                    extension    = ext,
                    lastModified = file.lastModified(),
                    size         = file.length(),
                    language     = ext.toCodeLanguage()
                )
            }
            .sortedWith(compareBy({ it.path }, { it.name }))
            .toList()
    }

    fun timeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60_000L       -> "just now"
            diff < 3_600_000L    -> "${diff / 60_000L}m ago"
            diff < 86_400_000L   -> "${diff / 3_600_000L}h ago"
            diff < 604_800_000L  -> "${diff / 86_400_000L}d ago"
            else                 -> "${diff / 604_800_000L}w ago"
        }
    }
}