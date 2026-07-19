package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.FireAmber
import com.example.myapplication.ui.theme.MythicGold

@Composable
fun EmbarkButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "compass_pulse")

    // 整个按钮随着古老契约的共鸣微弱缩放
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(width = 240.dp, height = 72.dp),
        contentAlignment = Alignment.Center
    ) {
        // 后置发光阴影，模拟神圣法阵
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(1.1f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(FireAmber.copy(alpha = 0.25f), Color.Transparent),
                    ),
                    RunicOctagonShape()
                )
        )

        // 核心金属按钮
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RunicOctagonShape())
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MythicGold, FireAmber)
                    )
                )
                .border(2.dp, MythicGold, RunicOctagonShape())
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "◆ 踏入荒野 ◆",
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "（开启地城副本冒险）",
                    fontSize = 10.sp,
                    color = Color.Black.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Serif
                )
            }
        }
    }
}
