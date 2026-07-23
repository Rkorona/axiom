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
 * Initialises sora-editor's TextMate pipeline and vends per-language
 * [TextMateLanguage] instances to the editor.
 *
 * All grammar and theme files live under `assets/textmate/` and are
 * loaded via [AssetsFileResolver] — no in-memory string injection.
 *
 * Call [init] once (idempotent) before the first [CodeEditor] is created.
 * Then call [createLanguage] and [applyTheme] for each editor instance.
 */
object TextMateManager {

    @Volatile private var initialized = false

    // ── Language → TextMate scope mapping ────────────────────────────────────
    private val CodeLanguage.textMateScope: String?
        get() = when (this) {
            CodeLanguage.KOTLIN     -> "source.kotlin"
            CodeLanguage.JAVA       -> "source.java"
            CodeLanguage.PYTHON     -> "source.python"
            CodeLanguage.JAVASCRIPT -> "source.js"
            CodeLanguage.TYPESCRIPT -> "source.ts"
            CodeLanguage.RUST       -> "source.rust"
            CodeLanguage.CPP        -> "source.cpp"
            CodeLanguage.C          -> "source.cpp"          // shares C++ grammar
            CodeLanguage.GO         -> "source.go"
            CodeLanguage.HTML       -> "text.html.basic"
            CodeLanguage.CSS        -> "source.css"
            CodeLanguage.JSON       -> "source.json"
            CodeLanguage.XML        -> "text.xml"
            CodeLanguage.MARKDOWN   -> "text.html.markdown"
            CodeLanguage.YAML       -> "source.yaml"
            CodeLanguage.SHELL      -> "source.shell"
            CodeLanguage.SWIFT      -> null                  // no bundled grammar
            CodeLanguage.DART       -> null                  // no bundled grammar
            CodeLanguage.UNKNOWN    -> null
        }

    // ── Initialisation ────────────────────────────────────────────────────────

    @Synchronized
    fun init(context: Context) {
        if (initialized) return
        try {
            // 1. Register the asset resolver — all paths are relative to assets/
            FileProviderRegistry.getInstance()
                .addFileProvider(AssetsFileResolver(context.applicationContext.assets))

            // 2. Load and activate the Axiom dark theme
            val themeRegistry = ThemeRegistry.getInstance()
            context.applicationContext.assets
                .open("textmate/themes/axiom-dark.json")
                .use { stream ->
                    themeRegistry.loadTheme(
                        ThemeModel(
                            IThemeSource.fromInputStream(
                                stream, "textmate/themes/axiom-dark.json", null
                            ),
                            "axiom-dark"
                        )
                    )
                }
            themeRegistry.setTheme("axiom-dark")

            // 3. Register all language grammars from the index file
            GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")

            initialized = true
        } catch (_: Exception) {
            // Falls back to AxiomEditorColorScheme / EmptyLanguage
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns a [TextMateLanguage] for [lang], or [EmptyLanguage] if TextMate
     * is not yet initialised or the language has no bundled grammar.
     *
     * @param autoCompleteEnabled When false, disables the completion popup and
     *   bracket auto-closing provided by the TextMate language pipeline.
     */
    fun createLanguage(lang: CodeLanguage, autoCompleteEnabled: Boolean = true): Language {
        if (!initialized) return EmptyLanguage()
        val scope = lang.textMateScope ?: return EmptyLanguage()
        return try {
            TextMateLanguage.create(scope, autoCompleteEnabled)
        } catch (_: Exception) {
            EmptyLanguage()
        }
    }

    /**
     * Applies [TextMateColorScheme] to [editor] so token colours match the
     * loaded theme.  Falls back to [AxiomEditorColorScheme] if TextMate is
     * not yet ready.
     */
    fun applyTheme(editor: CodeEditor) {
        editor.colorScheme = if (initialized) {
            try {
                TextMateColorScheme.create(ThemeRegistry.getInstance())
            } catch (_: Exception) {
                AxiomEditorColorScheme()
            }
        } else {
            AxiomEditorColorScheme()
        }
    }
}
