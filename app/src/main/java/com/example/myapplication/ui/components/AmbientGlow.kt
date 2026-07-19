package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.theme.FireAmber

/**
 * 模拟酒馆壁炉篝火微弱摇曳的氛围光晕
 */
@Composable
fun AmbientHearthGlow(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "harness_fire")

    // 动画化渐变光的半径与不透明度，模拟炉火燃烧的忽明忽暗
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

        // 篝火的光芒主要集中在底部中心
        val centerOffset = Offset(x = width / 2f, y = height * 0.9f)
        val radius = (width * 0.7f) * pulseScale

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    FireAmber.copy(alpha = alphaScale),
                    FireAmber.copy(alpha = alphaScale * 0.3f),
                    Color.Transparent
                ),
                center = centerOffset,
                radius = radius
            )
        )
    }
}
