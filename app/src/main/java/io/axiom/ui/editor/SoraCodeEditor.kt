package io.axiom.ui.editor

import android.graphics.Typeface
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.axiom.data.model.CodeLanguage
import io.axiom.data.model.EditorSettings
import io.axiom.ui.editor.textmate.TextMateManager
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.widget.CodeEditor

/**
 * Compose wrapper around sora-editor's [CodeEditor] with TextMate syntax highlighting.
 *
 * Settings are applied reactively:
 * - [EditorSettings.fontSize], [tabSize], [wordWrap], [lineNumbers] are applied on
 *   every `update` pass — cheap property assignments that take effect immediately.
 * - [EditorSettings.autoIndent] controls `autoCompleteEnabled` on the TextMate language
 *   (governs the completion popup and grammar-driven auto-indent on Enter).
 * - [EditorSettings.bracketPairs] controls `editor.props.symbolPairAutoCompletion`
 *   (governs whether typing `{` inserts a matching `}`).
 * - The language is recreated whenever [autoIndent], [tabSize], or the active file changes
 *   so the language picks up the current [tabWidth] as its indent unit.
 *
 * File switching:
 * - Content and language are only reloaded when [fileKey] changes, never on
 *   every recomposition or keystroke — this prevents cursor-jump on typing.
 */
@Composable
fun SoraCodeEditor(
    content:         String,
    fileKey:         String,
    language:        CodeLanguage,
    onContentChange: (String) -> Unit,
    settings:        EditorSettings = EditorSettings(),
    modifier:        Modifier       = Modifier
) {
    val context        = LocalContext.current
    val onChangeState  = rememberUpdatedState(onContentChange)

    // Track the last loaded file so we only reload content on file changes.
    var lastFileKey by remember { mutableStateOf<String?>(null) }
    // Track language-affecting settings so we recreate the language when they change.
    var lastLangKey by remember { mutableStateOf<String?>(null) }

    remember(context) { TextMateManager.init(context) }

    AndroidView(
        factory = { ctx ->
            TextMateManager.init(ctx)

            CodeEditor(ctx).apply {

                // ── Theme ──────────────────────────────────────────────────────
                TextMateManager.applyTheme(this)

                // ── Typography (initial values from settings) ──────────────────
                typefaceText = Typeface.MONOSPACE
                setTextSize(settings.fontSize.toFloat())
                tabWidth = settings.tabSize

                // ── Layout behaviour ───────────────────────────────────────────
                isWordwrap          = settings.wordWrap
                isLineNumberEnabled = settings.lineNumbers
                nonPrintablePaintingFlags = 0

                // ── Symbol pairs (bracket auto-close) ─────────────────────────
                // bracketPairs controls whether typing `{` inserts a matching `}`.
                // This is independent of syntax highlighting and auto-indent.
                props.symbolPairAutoCompletion = settings.bracketPairs

                // ── Scrollbars ─────────────────────────────────────────────────
                isVerticalScrollBarEnabled   = false
                isHorizontalScrollBarEnabled = false
                overScrollMode               = View.OVER_SCROLL_NEVER

                // ── Language ───────────────────────────────────────────────────
                // autoIndent → autoCompleteEnabled: controls the grammar-driven
                // indent engine and the completion popup inside TextMateLanguage.
                // tabWidth must be set BEFORE createLanguage so the language
                // picks it up as the indent unit for new blocks.
                setEditorLanguage(
                    TextMateManager.createLanguage(
                        lang                = language,
                        autoCompleteEnabled = settings.autoIndent
                    )
                )

                // ── Content change → ViewModel ─────────────────────────────────
                subscribeEvent(ContentChangeEvent::class.java) { _, _ ->
                    onChangeState.value(text.toString())
                }
            }
        },
        update = { editor ->

            // ── Live settings — safe to apply on every update ──────────────────
            editor.setTextSize(settings.fontSize.toFloat())
            editor.tabWidth             = settings.tabSize
            editor.isWordwrap           = settings.wordWrap
            editor.isLineNumberEnabled  = settings.lineNumbers

            // bracketPairs: symbol-pair auto-close is a props flag, no language
            // recreation needed — takes effect immediately on the next keystroke.
            editor.props.symbolPairAutoCompletion = settings.bracketPairs

            // ── File change: reload content, theme, and language ───────────────
            val fileChanged = fileKey != lastFileKey
            if (fileChanged) {
                lastFileKey = fileKey
                editor.setText(content)
                if (editor.lineCount > 0) editor.setSelection(0, 0)
                TextMateManager.applyTheme(editor)
            }

            // ── Language change: recreate when autoIndent, tabSize, or file changes.
            // tabSize is included so the new language is constructed after tabWidth
            // is already updated above — guaranteeing it uses the correct indent unit.
            val langKey = "$fileKey|${settings.autoIndent}|${settings.tabSize}|${language.name}"
            if (langKey != lastLangKey) {
                lastLangKey = langKey
                editor.setEditorLanguage(
                    TextMateManager.createLanguage(
                        lang                = language,
                        autoCompleteEnabled = settings.autoIndent
                    )
                )
            }
        },
        onRelease = { editor -> editor.release() },
        modifier  = modifier
    )
}
