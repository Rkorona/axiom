package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MythicGold

@Composable
fun InteractiveNodes(
    gold: Int,
    curableCurses: Int,
    onRest: () -> Unit,
    onBuyElixir: () -> Unit,
    onCureCurse: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedNode by remember { mutableStateOf<Int?>(null) } // 0: 吧台, 1: 铁匠铺, 2: 神殿

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. 酒馆吧台
        NodeCard(
            title = "🪵 橡木吧台",
            summary = "打听秘闻或支付 15🪙 饮酒长休，回复所有精力",
            isExpanded = expandedNode == 0,
            onClick = { expandedNode = if (expandedNode == 0) null else 0 }
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = "红脸老板拍了拍肮脏的桌板：\"喝一瓶雷霆麦酒，然后在炉火旁美美睡一觉，什么疲惫都消散了。\"",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    OutlinedButton(
                        onClick = onRest,
                        enabled = gold >= 15,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MythicGold),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                    ) {
                        Text("长休整备 (15 🪙)", fontSize = 12.sp, fontFamily = FontFamily.Serif)
                    }
                }
            }
        }

        // 2. 货郎与铁匠
        NodeCard(
            title = "⚒️ 流浪铁匠与货郎",
            summary = "购买疗伤药水或修整钝化的兵刃",
            isExpanded = expandedNode == 1,
            onClick = { expandedNode = if (expandedNode == 1) null else 1 }
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = "货郎摇晃着皮袋里的红色液体：\"这是刚从废墟边缘收来的野草熬出来的，保命用它最灵！\"",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    OutlinedButton(
                        onClick = onBuyElixir,
                        enabled = gold >= 20,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MythicGold)
                    ) {
                        Text("买炼金药水 (20 🪙)", fontSize = 12.sp, fontFamily = FontFamily.Serif)
                    }
                }
            }
        }

        // 3. 破败神龛
        NodeCard(
            title = "⛪ 丰收与星辰神龛",
            summary = "祈求净化驱散负面效果，每次需奉献 30🪙",
            isExpanded = expandedNode == 2,
            onClick = { expandedNode = if (expandedNode == 2) null else 2 }
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = "布满苔藓的星宿石雕散发着微光。修女低头颂念圣词，等待你的献祭。",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                if (curableCurses > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⚠️ 你身上缠绕着 $curableCurses 个无法自我治愈的诅咒！",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = FontFamily.Serif
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    OutlinedButton(
                        onClick = onCureCurse,
                        enabled = gold >= 30 && curableCurses > 0,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MythicGold)
                    ) {
                        Text("驱散诅咒 (30 🪙)", fontSize = 12.sp, fontFamily = FontFamily.Serif)
                    }
                }
            }
        }
    }
}

/**
 * 具有弹性伸缩动效的自定义互动卡片
 */
@Composable
private fun NodeCard(
    title: String,
    summary: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), ScrollCutShape(12f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), ScrollCutShape(12f))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Serif,
                    color = MythicGold
                )
                if (!isExpanded) {
                    Text(
                        text = summary,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                text = if (isExpanded) "▲" else "▼",
                fontSize = 10.sp,
                color = MythicGold
            )
        }

        // 使用 Expressive 特色的物理弹性展开动画
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = spring(dampingRatio = 0.75f, stiffness = 150f)),
            exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f))
        ) {
            content()
        }
    }
}
