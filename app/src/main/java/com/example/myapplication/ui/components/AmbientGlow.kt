package com.example.myapplication.ui.components

import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.theme.ArcanePrimary
import com.example.myapplication.ui.theme.EmberOrange

/**
 * 模拟酒馆壁炉篝火与秘法微光交织摇曳的氛围光晕。
 * 双光源替代原先单一炉火光，呼应 Expressive 更丰富的色彩层次。
 */
@Composable
fun AmbientHearthGlow(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val alphaScale by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 炉火光源：集中在底部中心
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    EmberOrange.copy(alpha = alphaScale),
                    EmberOrange.copy(alpha = alphaScale * 0.3f),
                    Color.Transparent
                ),
                center = Offset(x = width / 2f, y = height * 0.9f),
                radius = (width * 0.7f) * pulseScale
            )
        )

        // 秘法光源：顶部左侧的紫色微光，增加 Expressive 层次感
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    ArcanePrimary.copy(alpha = alphaScale * 0.6f),
                    Color.Transparent
                ),
                center = Offset(x = width * 0.15f, y = 0f),
                radius = (width * 0.6f) * pulseScale
            )
        )
    }
}
