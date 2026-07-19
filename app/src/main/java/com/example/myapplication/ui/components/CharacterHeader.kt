package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.CharacterStats
import com.example.myapplication.ui.theme.DarkRuby
import com.example.myapplication.ui.theme.MagicMana
import com.example.myapplication.ui.theme.MythicGold

@Composable
fun CharacterHeader(
    stats: CharacterStats,
    day: Int,
    timeLabel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RunicOctagonShape())
            .border(1.dp, MythicGold.copy(alpha = 0.6f), RunicOctagonShape())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：等级与头像徽章
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MythicGold.copy(alpha = 0.15f), RunicOctagonShape())
                .border(1.dp, MythicGold, RunicOctagonShape()),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LV",
                    fontSize = 10.sp,
                    color = MythicGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stats.level}",
                    fontSize = 18.sp,
                    color = MythicGold,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 中间：血条、蓝条与金币
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${stats.name} · ${stats.title}",
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            // 生命条
            RunicBar(
                current = stats.hp,
                max = stats.maxHp,
                label = "HP",
                color = DarkRuby
            )
            Spacer(modifier = Modifier.height(3.dp))

            // 魔法条
            RunicBar(
                current = stats.mp,
                max = stats.maxMp,
                label = "MP",
                color = MagicMana
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 右侧：时间与天数系统
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "第 $day 天",
                fontFamily = FontFamily.Serif,
                color = MythicGold,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = timeLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            // 资产显示
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🪙 ${stats.gold}",
                    color = MythicGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 具有跑团古典气质的状态槽
 */
@Composable
private fun RunicBar(
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
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(22.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.background)
                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$current/$max",
            fontSize = 9.sp,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}
