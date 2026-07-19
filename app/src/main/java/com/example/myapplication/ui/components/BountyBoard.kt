package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.BountyQuest
import com.example.myapplication.ui.theme.DarkParchment
import com.example.myapplication.ui.theme.MythicGold

@Composable
fun BountyBoard(
    quests: List<BountyQuest>,
    onAcceptQuest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkParchment.copy(alpha = 0.8f), ScrollCutShape(16f))
            .border(1.dp, MythicGold.copy(alpha = 0.3f), ScrollCutShape(16f))
            .padding(16.dp)
    ) {
        Text(
            text = "📜 冒险者行会布告栏",
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            color = MythicGold,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        quests.forEach { quest ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), ScrollCutShape(8f))
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), ScrollCutShape(8f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = quest.title,
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // 难度标志
                        Text(
                            text = "[${quest.difficulty}]",
                            fontSize = 10.sp,
                            color = when (quest.difficulty) {
                                "噩梦" -> MaterialTheme.colorScheme.error
                                "困难" -> MaterialTheme.colorScheme.secondary
                                else -> MythicGold
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = quest.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "赏金: 🪙 ${quest.reward}",
                        fontSize = 11.sp,
                        color = MythicGold,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }

                // 揭下悬赏按钮
                TextButton(
                    onClick = { onAcceptQuest(quest.id) },
                    enabled = !quest.isAccepted,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MythicGold,
                        disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = if (quest.isAccepted) "已揭下" else "揭下悬赏",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
