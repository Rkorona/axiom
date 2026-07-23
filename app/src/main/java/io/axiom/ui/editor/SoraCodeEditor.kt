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
 * - [EditorSettings.autoIndent] and [bracketPairs] control `autoCompleteEnabled`
 *   on the TextMate language; together they govern the completion popup and
 *   bracket auto-closing. The language is recreated whenever either flag or the
 *   active file changes.
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
                isWordwrap        = settings.wordWrap
                isLineNumberEnabled = settings.lineNumbers
                nonPrintablePaintingFlags = 0

                // ── Scrollbars ─────────────────────────────────────────────────
                isVerticalScrollBarEnabled   = false
                isHorizontalScrollBarEnabled = false
                overScrollMode               = View.OVER_SCROLL_NEVER

                // ── Language ───────────────────────────────────────────────────
                setEditorLanguage(
                    TextMateManager.createLanguage(
                        lang                = language,
                        autoCompleteEnabled = settings.autoIndent && settings.bracketPairs
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

            // ── File change: reload content, theme, and language ───────────────
            val fileChanged = fileKey != lastFileKey
            if (fileChanged) {
                lastFileKey = fileKey
                editor.setText(content)
                if (editor.lineCount > 0) editor.setSelection(0, 0)
                TextMateManager.applyTheme(editor)
            }

            // ── Language change: recreate when file or completion flags change ──
            val langKey = "$fileKey|${settings.autoIndent}|${settings.bracketPairs}|${language.name}"
            if (langKey != lastLangKey) {
                lastLangKey = langKey
                editor.setEditorLanguage(
                    TextMateManager.createLanguage(
                        lang                = language,
                        autoCompleteEnabled = settings.autoIndent && settings.bracketPairs
                    )
                )
            }
        },
        onRelease = { editor -> editor.release() },
        modifier  = modifier
    )
}
