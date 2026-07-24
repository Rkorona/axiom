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
 * Indentation is hardcoded to 2 spaces (`tabWidth = 2`).
 *
 * Settings are applied **only when their value changes** to prevent sora-editor from
 * calling `invalidate()` on every keystroke (which caused the editor to flash).
 *
 * Auto-indent is always enabled (`editor.props.autoIndent = true`).
 *
 * File switching:
 * Content and language are only reloaded when [fileKey] changes, never on every
 * recomposition or keystroke — this prevents cursor-jump and flash on typing.
 */
@Composable
fun SoraCodeEditor(
    content:              String,
    fileKey:              String,
    language:             CodeLanguage,
    onContentChange:      (String) -> Unit,
    settings:             EditorSettings          = EditorSettings(),
    controller:           SoraEditorController?   = null,
    onEditorFocusChange:  ((Boolean) -> Unit)?    = null,
    modifier:             Modifier                = Modifier
) {
    val context          = LocalContext.current
    val onChangeState    = rememberUpdatedState(onContentChange)
    val onFocusState     = rememberUpdatedState(onEditorFocusChange)

    var lastFileKey  by remember { mutableStateOf<String?>(null) }
    var lastLangKey  by remember { mutableStateOf<String?>(null) }
    // Track the last applied settings so we skip sora-editor property writes (which
    // call invalidate() internally) when the value hasn't actually changed.
    var lastSettings by remember { mutableStateOf<EditorSettings?>(null) }

    remember(context) { TextMateManager.init(context) }

    AndroidView(
        factory = { ctx ->
            TextMateManager.init(ctx)

            CodeEditor(ctx).apply {
                controller?.attach(this)

                // ── Theme ──────────────────────────────────────────────────────
                TextMateManager.applyTheme(this)

                // ── Typography ─────────────────────────────────────────────────
                typefaceText = Typeface.MONOSPACE
                setTextSize(settings.fontSize.toFloat())
                tabWidth = 2

                // ── Layout behaviour ───────────────────────────────────────────
                isWordwrap          = settings.wordWrap
                isLineNumberEnabled = settings.lineNumbers
                nonPrintablePaintingFlags = 0

                // ── Auto-indent always on ──────────────────────────────────────
                props.autoIndent = true

                // ── Scrollbars ─────────────────────────────────────────────────
                isVerticalScrollBarEnabled   = false
                isHorizontalScrollBarEnabled = false
                overScrollMode               = View.OVER_SCROLL_NEVER

                // ── Language ───────────────────────────────────────────────────
                setEditorLanguage(TextMateManager.createLanguage(language))

                // ── Editor focus → ViewModel ───────────────────────────────────
                setOnFocusChangeListener { _, hasFocus ->
                    onFocusState.value?.invoke(hasFocus)
                }

                // ── Content change → ViewModel ─────────────────────────────────
                subscribeEvent(ContentChangeEvent::class.java) { _, _ ->
                    onChangeState.value(text.toString())
                }
            }
        },
        update = { editor ->

            val prev = lastSettings

            // Only write properties whose value actually changed.
            // Sora-editor setters call invalidate() even for no-op writes,
            // causing a visible flash on every keystroke if applied unconditionally.
            if (prev == null || prev.fontSize != settings.fontSize)
                editor.setTextSize(settings.fontSize.toFloat())
            if (prev == null || prev.wordWrap != settings.wordWrap)
                editor.isWordwrap = settings.wordWrap
            if (prev == null || prev.lineNumbers != settings.lineNumbers)
                editor.isLineNumberEnabled = settings.lineNumbers

            lastSettings = settings

            // ── File change: reload content and theme ──────────────────────────
            val fileChanged = fileKey != lastFileKey
            if (fileChanged) {
                lastFileKey = fileKey
                editor.setText(content)
                if (editor.lineCount > 0) editor.setSelection(0, 0)
                TextMateManager.applyTheme(editor)
            }

            // ── Language change: recreate only when file or language type changes.
            val langKey = "$fileKey|${language.name}"
            if (langKey != lastLangKey) {
                lastLangKey = langKey
                editor.setEditorLanguage(TextMateManager.createLanguage(language))
            }
        },
        onRelease = { editor ->
            controller?.detach()
            editor.release()
        },
        modifier  = modifier
    )
}
