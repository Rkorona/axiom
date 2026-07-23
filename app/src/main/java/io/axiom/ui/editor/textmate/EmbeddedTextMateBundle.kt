package io.axiom.ui.editor.textmate

import io.github.rosemoe.sora.langs.textmate.registry.provider.FileResolver
import java.io.ByteArrayInputStream
import java.io.InputStream

object InMemoryFileResolver : FileResolver {
    private val files = mutableMapOf<String, ByteArray>()

    fun registerFile(path: String, content: String) {
        files[path] = content.toByteArray(Charsets.UTF_8)
    }

    override fun resolveStreamByPath(path: String): InputStream? {
        val bytes = files[path] ?: return null
        return ByteArrayInputStream(bytes)
    }
}

object EmbeddedTextMateBundle {

    fun install() {
        // 1. 语言索引定义
        InMemoryFileResolver.registerFile(
            "textmate/embedded/languages.json",
            """
            {
              "languages": [
                { "name": "javascript", "scopeName": "source.js",     "grammar": "textmate/embedded/javascript.json" },
                { "name": "typescript", "scopeName": "source.ts",     "grammar": "textmate/embedded/javascript.json" },
                { "name": "kotlin",     "scopeName": "source.kotlin", "grammar": "textmate/embedded/kotlin.json" },
                { "name": "python",     "scopeName": "source.python", "grammar": "textmate/embedded/python.json" },
                { "name": "java",       "scopeName": "source.java",   "grammar": "textmate/embedded/java.json" },
                { "name": "json",       "scopeName": "source.json",   "grammar": "textmate/embedded/json.json" },
                { "name": "shell",      "scopeName": "source.shell",  "grammar": "textmate/embedded/shell.json" }
              ]
            }
            """.trimIndent()
        )

        // 2. 深空主题 Token 颜色定义
        InMemoryFileResolver.registerFile(
            "textmate/embedded/darcula.json",
            """
            {
              "name": "darcula",
              "type": "dark",
              "colors": {
                "editor.background": "#0C0E14",
                "editor.foreground": "#EAEBF5"
              },
              "tokenColors": [
                {
                  "name": "JSON Keys & Properties",
                  "scope": ["string.quoted.double.json", "support.type.property-name.json"],
                  "settings": { "foreground": "#B0A4FF", "fontStyle": "bold" }
                },
                {
                  "name": "Strings",
                  "scope": ["string", "string.quoted", "string.template"],
                  "settings": { "foreground": "#00DDB3" }
                },
                {
                  "name": "Numbers & Constants",
                  "scope": ["constant.numeric", "constant.language"],
                  "settings": { "foreground": "#FF6B8A" }
                },
                {
                  "name": "Keywords",
                  "scope": ["keyword", "storage.type", "storage.modifier", "keyword.control"],
                  "settings": { "foreground": "#7B68EE", "fontStyle": "bold" }
                },
                {
                  "name": "Functions & Methods",
                  "scope": ["entity.name.function", "support.function"],
                  "settings": { "foreground": "#B0A4FF" }
                },
                {
                  "name": "Comments",
                  "scope": ["comment", "punctuation.definition.comment"],
                  "settings": { "foreground": "#4A4D68", "fontStyle": "italic" }
                }
              ]
            }
            """.trimIndent()
        )

        // 3. JSON 语法规则
        InMemoryFileResolver.registerFile(
            "textmate/embedded/json.json",
            """
            {
              "scopeName": "source.json",
              "patterns": [
                {
                  "name": "string.quoted.double.json",
                  "begin": "\"",
                  "end": "\"",
                  "patterns": [
                    { "name": "constant.character.escape.json", "match": "\\\\." }
                  ]
                },
                { "name": "constant.numeric.json", "match": "\\b[0-9]+(\\.[0-9]+)?\\b" },
                { "name": "constant.language.json", "match": "\\b(true|false|null)\\b" }
              ]
            }
            """.trimIndent()
        )

        // 4. JavaScript 语法规则
        InMemoryFileResolver.registerFile(
            "textmate/embedded/javascript.json",
            """
            {
              "scopeName": "source.js",
              "patterns": [
                { "name": "comment.line.double-slash.js", "match": "//.*$" },
                { "name": "comment.block.js", "begin": "/\\*", "end": "\\*/" },
                { "name": "string.quoted.double.js", "begin": "\"", "end": "\"" },
                { "name": "string.quoted.single.js", "begin": "'", "end": "'" },
                { "name": "string.quoted.template.js", "begin": "`", "end": "`" },
                { "name": "constant.numeric.js", "match": "\\b[0-9]+(\\.[0-9]+)?\\b" },
                { "name": "keyword.control.js", "match": "\\b(let|var|const|function|return|if|else|for|while|do|class|extends|import|export|from|async|await|try|catch|finally|throw|new|this|of|in|switch|case|break|continue|typeof|instanceof)\\b" },
                { "name": "constant.language.js", "match": "\\b(true|false|null|undefined|NaN|Infinity)\\b" },
                { "name": "support.function.js", "match": "\\b(console|log|warn|error|info|push|pop|map|filter|reduce|forEach|indexOf|substring|parseInt|parseFloat)\\b" }
              ]
            }
            """.trimIndent()
        )

        // 5. Kotlin 语法规则
        InMemoryFileResolver.registerFile(
            "textmate/embedded/kotlin.json",
            """
            {
              "scopeName": "source.kotlin",
              "patterns": [
                { "name": "comment.line.double-slash.kt", "match": "//.*$" },
                { "name": "comment.block.kt", "begin": "/\\*", "end": "\\*/" },
                { "name": "string.quoted.double.kt", "begin": "\"", "end": "\"" },
                { "name": "constant.numeric.kt", "match": "\\b[0-9]+(\\.[0-9]+)?[fFL]?\\b" },
                { "name": "keyword.control.kt", "match": "\\b(val|var|fun|class|object|interface|enum|sealed|data|package|import|return|if|else|when|for|while|do|try|catch|finally|throw|public|private|protected|internal|companion|override|open|abstract|typealias)\\b" },
                { "name": "constant.language.kt", "match": "\\b(true|false|null|this|super)\\b" },
                { "name": "support.function.kt", "match": "\\b(println|print|listOf|mapOf|setOf|mutableListOf|apply|let|also|run|with)\\b" }
              ]
            }
            """.trimIndent()
        )

        // 6. Python 语法规则
        InMemoryFileResolver.registerFile(
            "textmate/embedded/python.json",
            """
            {
              "scopeName": "source.python",
              "patterns": [
                { "name": "comment.line.number-sign.py", "match": "#.*$" },
                { "name": "string.quoted.double.py", "begin": "\"", "end": "\"" },
                { "name": "string.quoted.single.py", "begin": "'", "end": "'" },
                { "name": "constant.numeric.py", "match": "\\b[0-9]+(\\.[0-9]+)?\\b" },
                { "name": "keyword.control.py", "match": "\\b(def|class|return|if|elif|else|for|while|try|except|finally|raise|import|from|as|pass|break|continue|with|lambda|async|await|global|nonlocal)\\b" },
                { "name": "constant.language.py", "match": "\\b(True|False|None|self)\\b" },
                { "name": "support.function.py", "match": "\\b(print|len|range|enumerate|zip|isinstance|str|int|float|list|dict|set)\\b" }
              ]
            }
            """.trimIndent()
        )

        // 7. Java 语法规则
        InMemoryFileResolver.registerFile(
            "textmate/embedded/java.json",
            """
            {
              "scopeName": "source.java",
              "patterns": [
                { "name": "comment.line.double-slash.java", "match": "//.*$" },
                { "name": "comment.block.java", "begin": "/\\*", "end": "\\*/" },
                { "name": "string.quoted.double.java", "begin": "\"", "end": "\"" },
                { "name": "constant.numeric.java", "match": "\\b[0-9]+(\\.[0-9]+)?[fFL]?\\b" },
                { "name": "keyword.control.java", "match": "\\b(public|private|protected|static|final|class|interface|enum|extends|implements|void|int|double|float|long|boolean|char|byte|short|return|if|else|for|while|do|switch|case|try|catch|finally|throw|throws|new|import|package)\\b" },
                { "name": "constant.language.java", "match": "\\b(true|false|null|this|super)\\b" }
              ]
            }
            """.trimIndent()
        )

        // 8. Shell 语法规则
        InMemoryFileResolver.registerFile(
            "textmate/embedded/shell.json",
            """
            {
              "scopeName": "source.shell",
              "patterns": [
                { "name": "comment.line.number-sign.sh", "match": "#.*$" },
                { "name": "string.quoted.double.sh", "begin": "\"", "end": "\"" },
                { "name": "string.quoted.single.sh", "begin": "'", "end": "'" },
                { "name": "constant.numeric.sh", "match": "\\b[0-9]+\\b" },
                { "name": "keyword.control.sh", "match": "\\b(if|then|else|elif|fi|case|esac|for|while|until|do|done|in|function|return|exit|export|unset|local)\\b" }
              ]
            }
            """.trimIndent()
        )
    }
}