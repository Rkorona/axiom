package io.axiom

import android.app.Application
import io.axiom.ui.editor.textmate.TextMateManager

/**
 * Axiom 全局 Application。
 * 负责在应用启动时预热全局资源（如 TextMate 语法规则与内置深空主题包）。
 */
class AxiomApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 全局预热 TextMate 内存语法树与主题包
        TextMateManager.ensureInitialized(this)
    }
}