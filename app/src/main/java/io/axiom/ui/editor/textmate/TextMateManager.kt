package io.axiom.ui.editor.textmate

import android.content.Context
import io.axiom.data.model.CodeLanguage
import io.axiom.ui.editor.AxiomEditorColorScheme
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.CodeEditor
import org.eclipse.tm4e.core.registry.IThemeSource

/**
 * TextMate 语法高亮与主题全局管理器。
 */
object TextMateManager {

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isThemeLoaded = false

    fun CodeLanguage.toTextMateScope(): String? = when (this) {
        CodeLanguage.KOTLIN     -> "source.kotlin"
        CodeLanguage.JAVA       -> "source.java"
        CodeLanguage.PYTHON     -> "source.python"
        CodeLanguage.JAVASCRIPT -> "source.js"
        CodeLanguage.TYPESCRIPT -> "source.ts"
        CodeLanguage.RUST       -> "source.js"
        CodeLanguage.CPP        -> "source.java"
        CodeLanguage.C          -> "source.java"
        CodeLanguage.GO         -> "source.js"
        CodeLanguage.SWIFT      -> "source.kotlin"
        CodeLanguage.DART       -> "source.java"
        CodeLanguage.HTML       -> "source.js"
        CodeLanguage.CSS        -> "source.js"
        CodeLanguage.JSON       -> "source.json"
        CodeLanguage.XML        -> "source.json"
        CodeLanguage.MARKDOWN   -> "source.js"
        CodeLanguage.YAML       -> "source.json"
        CodeLanguage.SHELL      -> "source.shell"
        CodeLanguage.UNKNOWN    -> null
    }

    /**
     * 同步加载内存内置 TextMate 规则（耗时 <1ms），确保 View 首次挂载即具备高亮能力。
     */
    @Synchronized
    fun ensureInitialized(context: Context) {
        if (isInitialized) return

        runCatching {
            // 1. 安装内存内置语法包与主题包
            EmbeddedTextMateBundle.install()

            val fileRegistry = FileProviderRegistry.getInstance()
            fileRegistry.addFileProvider(InMemoryFileResolver)
            runCatching {
                fileRegistry.addFileProvider(AssetsFileResolver(context.applicationContext.assets))
            }

            // 2. 加载内置深空主题
            val themeRegistry = ThemeRegistry.getInstance()
            val themePath = "textmate/embedded/darcula.json"
            val themeStream = fileRegistry.tryGetInputStream(themePath)
            if (themeStream != null) {
                runCatching {
                    val themeModel = ThemeModel(
                        IThemeSource.fromInputStream(themeStream, themePath, null),
                        "darcula"
                    )
                    themeRegistry.loadTheme(themeModel)
                    themeRegistry.setTheme("darcula")
                    isThemeLoaded = true
                }
            }

            // 3. 加载内置核心语法注册表
            val grammarRegistry = GrammarRegistry.getInstance()
            grammarRegistry.loadGrammars("textmate/embedded/languages.json")

            // 4. 尝试加载用户可能放在 assets/textmate 中的扩展语法
            runCatching {
                grammarRegistry.loadGrammars("textmate/languages.json")
            }

            isInitialized = true
        }.onFailure {
            isInitialized = false
        }
    }

    fun createLanguageFor(language: CodeLanguage): Language {
        val scope = language.toTextMateScope() ?: return EmptyLanguage()
        if (!isInitialized) return EmptyLanguage()

        return runCatching {
            TextMateLanguage.create(
                scope,
                true /* autoCompleteEnabled */
            )
        }.getOrElse {
            EmptyLanguage()
        }
    }

    /**
     * 将 TextMateColorScheme 强制赋予编辑器。
     */
    fun applyColorScheme(editor: CodeEditor) {
        if (isThemeLoaded) {
            runCatching {
                editor.colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
                return
            }
        }
        editor.colorScheme = AxiomEditorColorScheme()
    }
}