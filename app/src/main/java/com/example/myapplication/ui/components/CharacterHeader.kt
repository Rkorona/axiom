package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.CharacterStats
import com.example.myapplication.ui.theme.ArcanePrimary
import com.example.myapplication.ui.theme.ArcanePrimaryDim
import com.example.myapplication.ui.theme.BloodRose
import com.example.myapplication.ui.theme.ManaTeal
import com.example.myapplication.ui.theme.TreasureGold
import com.example.myapplication.ui.theme.VoidBackground

@Composable
fun CharacterHeader(
    stats: CharacterStats,
    day: Int,
    timeLabel: String,
    onHeaderClick: () -> Unit, // 新增：点击头像栏的回调事件
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.Card)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .clickable { onHeaderClick() } // 新增：使顶部状态栏可以被点击
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：等级徽章 —— 圆角渐变 blob
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(ExpressiveShapes.Badge)
                .background(
                    Brush.linearGradient(
                        colors = listOf(ArcanePrimary, ArcanePrimaryDim)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LV",
                    fontSize = 9.sp,
                    color = VoidBackground.copy(alpha = 0.7f),
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${stats.level}",
                    fontSize = 22.sp,
                    color = VoidBackground,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 中间：姓名、血条、蓝条
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${stats.name} · ${stats.title}",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExpressiveBar(
                current = stats.hp,
                max = stats.maxHp,
                label = "HP",
                color = BloodRose
            )
            Spacer(modifier = Modifier.height(4.dp))

            ExpressiveBar(
                current = stats.mp,
                max = stats.maxMp,
                label = "MP",
                color = ManaTeal
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 右侧：时间、天数、金币
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "第 $day 天",
                color = ArcanePrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
            Text(
                text = timeLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(ExpressiveShapes.Pill)
                    .background(TreasureGold.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "🪙 ${stats.gold}",
                    color = TreasureGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * 表现力风格的胶囊状态槽
 */
@Composable
private fun ExpressiveBar(
    current: Int,
    max: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    val progress = if (max > 0) current.toFloat() / max else 0f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(0.95f)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            modifier = Modifier.width(22.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(ExpressiveShapes.Pill)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(ExpressiveShapes.Pill)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(color, color.copy(alpha = 0.7f))
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$current/$max",
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}