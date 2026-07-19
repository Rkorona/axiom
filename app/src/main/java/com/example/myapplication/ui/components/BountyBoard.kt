package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.BountyQuest
import com.example.myapplication.ui.theme.ArcanePrimary
import com.example.myapplication.ui.theme.TreasureGold
import com.example.myapplication.ui.theme.VoidBackground

@Composable
fun BountyBoard(
    quests: List<BountyQuest>,
    onAcceptQuest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.CardMedium)
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(ExpressiveShapes.Badge)
                    .background(ArcanePrimary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📜", fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "冒险者行会布告栏",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
        }

        quests.forEachIndexed { index, quest ->
            QuestCard(quest = quest, onAcceptQuest = onAcceptQuest)
            if (index != quests.lastIndex) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun QuestCard(
    quest: BountyQuest,
    onAcceptQuest: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.ChipInner)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = quest.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(8.dp))

            val tagColor = when (quest.difficulty) {
                "噩梦" -> MaterialTheme.colorScheme.error
                "困难" -> MaterialTheme.colorScheme.secondary
                else -> ArcanePrimary
            }
            Box(
                modifier = Modifier
                    .clip(ExpressiveShapes.Pill)
                    .background(tagColor.copy(alpha = 0.18f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = quest.difficulty,
                    fontSize = 10.sp,
                    color = tagColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = quest.description,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "赏金 🪙 ${quest.reward}",
                fontSize = 13.sp,
                color = TreasureGold,
                fontWeight = FontWeight.ExtraBold
            )

            if (quest.isAccepted) {
                Text(
                    text = "已揭下",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                Button(
                    onClick = { onAcceptQuest(quest.id) },
                    shape = ExpressiveShapes.Pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = VoidBackground
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("揭下悬赏", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
