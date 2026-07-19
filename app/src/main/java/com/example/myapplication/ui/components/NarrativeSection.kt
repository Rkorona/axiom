package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.TimeOfDay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NarrativeSection(
    timeOfDay: TimeOfDay,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), ScrollCutShape())
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), ScrollCutShape())
            .padding(16.dp)
    ) {
        Text(
            text = "酒馆见闻",
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Serif,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // 时间轮转时的淡入淡出动画
        AnimatedContent(
            targetState = timeOfDay,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "text_narrative"
        ) { state ->
            Text(
                text = state.description,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                )
            )
        }
    }
}
