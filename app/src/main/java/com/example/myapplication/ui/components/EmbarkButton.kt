package com.example.myapplication.ui.components

import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.ArcanePrimary
import com.example.myapplication.ui.theme.VoidBackground

/**
 * 具有按压回弹与物理触觉震动反馈的“出征”按钮。
 * 融合了 M3 Expressive 的手势动态响应特征。
 */
@Composable
fun EmbarkButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 1. 无限循环的背景微弱共鸣呼吸
    val infiniteTransition = rememberInfiniteTransition(label = "embark_pulse")
    val idleScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_scale"
    )

    // 2. 玩家手指按下时的即时收缩反馈 (按下时收缩至 0.92f，松开后在 spring 作用下弹回)
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else idleScale,
        animationSpec = tween(durationMillis = 100),
        label = "press_scale"
    )

    // 3. 物理触觉效果：按下与抬起时，通过手机马达给予轻微触觉回馈
    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // 模拟卡扣卡入
        }
    }

    Box(
        modifier = modifier
            .scale(animatedScale)
            .fillMaxWidth()
            .height(72.dp)
            .clip(ExpressiveShapes.Card)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        ArcanePrimary,
                        Color(0xFFD6B8FF)
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 禁用默认现代水波纹，保持卡牌厚重干净感
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress) // 触发更强的确认震动
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "⚔️", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "出征荒野",
                fontSize = 17.sp,
                color = VoidBackground,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}