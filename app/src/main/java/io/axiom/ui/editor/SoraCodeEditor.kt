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
import io.axiom.ui.editor.textmate.TextMateManager
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.widget.CodeEditor

/**
 * 集成 TextMate 动态语法高亮与 TextMateColorScheme 的 Sora Editor 封装。
 */
@Composable
fun SoraCodeEditor(
    content: String,
    fileKey: String,
    language: CodeLanguage,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val onChangeState = rememberUpdatedState(onContentChange)

    var lastLoadedKey by remember { mutableStateOf<String?>(null) }

    // 同步确保持润 TextMate 环境准备就绪
    remember(context) {
        TextMateManager.ensureInitialized(context)
    }

    AndroidView(
        factory = { ctx ->
            TextMateManager.ensureInitialized(ctx)

            CodeEditor(ctx).apply {
                // 必须在 View 实例化时强绑定 TextMateColorScheme，高亮颜色才不会变成透明！
                TextMateManager.applyColorScheme(this)

                typefaceText = Typeface.MONOSPACE
                setTextSize(13f)
                tabWidth = 4

                isWordwrap = false
                isLineNumberEnabled = true
                nonPrintablePaintingFlags = 0

                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER

                // 设置语言语法解析器
                setEditorLanguage(TextMateManager.createLanguageFor(language))

                subscribeEvent(ContentChangeEvent::class.java) { _, _ ->
                    onChangeState.value(text.toString())
                }
            }
        },
        update = { editor ->
            if (fileKey != lastLoadedKey) {
                lastLoadedKey = fileKey
                editor.setText(content)
                if (editor.lineCount > 0) {
                    editor.setSelection(0, 0)
                }
                TextMateManager.applyColorScheme(editor)
                editor.setEditorLanguage(TextMateManager.createLanguageFor(language))
            }
        },
        onRelease = { editor ->
            editor.release()
        },
        modifier = modifier
    )
}