package com.example.myapplication.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * 远古八角形（用于核心按钮与神圣状态栏）
 */
class RunicOctagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val w = size.width
            val h = size.height
            val cx = w * 0.15f
            val cy = h * 0.15f

            moveTo(cx, 0f)
            lineTo(w - cx, 0f)
            lineTo(w, cy)
            lineTo(w, h - cy)
            lineTo(w - cx, h)
            lineTo(cx, h)
            lineTo(0f, h - cy)
            lineTo(0f, cy)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * 卷轴切角卡片（用于模拟羊皮纸或厚重皮革的切角）
 */
class ScrollCutShape(val cutSizeDp: Float = 24f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val w = size.width
            val h = size.height
            // 根据密度转换切角大小
            val cut = cutSizeDp * density.density

            moveTo(cut, 0f)
            lineTo(w - cut, 0f)
            lineTo(w, cut)
            lineTo(w, h - cut)
            lineTo(w - cut, h)
            lineTo(cut, h)
            lineTo(0f, h - cut)
            lineTo(0f, cut)
            close()
        }
        return Outline.Generic(path)
    }
}
