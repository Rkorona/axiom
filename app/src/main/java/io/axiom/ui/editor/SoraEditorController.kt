package io.axiom.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.rosemoe.sora.widget.CodeEditor

/**
 * Handle that lets external composables (e.g. [SymbolBar]) insert text at the
 * cursor of the currently active [CodeEditor] without holding a direct reference
 * to the View.
 *
 * Attach / detach lifecycle is managed by [SoraCodeEditor] via [attach] and [detach].
 */
class SoraEditorController {

    private var editor: CodeEditor? = null

    internal fun attach(editor: CodeEditor) { this.editor = editor }
    internal fun detach()                   { this.editor = null  }

    /**
     * Inserts [text] at the current cursor position and moves the cursor to the
     * end of the inserted text.  No-op when no editor is attached.
     */
    fun insertText(text: String) {
        val e = editor ?: return
        val cursor = e.cursor
        e.text.insert(cursor.leftLine, cursor.leftColumn, text)
    }
}

/** Creates and remembers a [SoraEditorController] tied to the current composition. */
@Composable
fun rememberSoraEditorController(): SoraEditorController = remember { SoraEditorController() }
