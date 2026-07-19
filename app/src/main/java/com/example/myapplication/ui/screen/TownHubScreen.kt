package com.example.myapplication.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BountyQuest
import com.example.myapplication.data.CharacterStats
import com.example.myapplication.data.GameState
import com.example.myapplication.data.TimeOfDay
import com.example.myapplication.ui.components.*

@Composable
fun TownHubScreen() {
    val context = LocalContext.current

    // 初始化本地模拟的跑团游戏数据
    var gameState by remember {
        mutableStateOf(
            GameState(
                character = CharacterStats(
                    name = "圣骑士 艾莉尔",
                    title = "星宿骑士",
                    hp = 42,
                    maxHp = 80,
                    mp = 15,
                    maxMp = 40,
                    gold = 68,
                    level = 3,
                    curableCurses = 1
                ),
                timeOfDay = TimeOfDay.EVENING,
                day = 4,
                bounties = listOf(
                    BountyQuest("q1", "尖啸林地的狼人首领", "困难", 120, "村民在西边林地发现了染疫的狼爪痕迹，需要白银武器持有者清剿。"),
                    BountyQuest("q2", "废弃矿井的幽灵低语", "噩梦", 350, "深藏地底的矿坑不断传来摄魂怪的哀嚎，靠近者都迷失了神智。")
                )
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. 背景动态炉火微光
            AmbientHearthGlow()

            // 2. 主页面内容滚动流
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 顶部玩家数据徽章
                CharacterHeader(
                    stats = gameState.character,
                    day = gameState.day,
                    timeLabel = gameState.timeOfDay.label
                )

                // 氛围叙事
                NarrativeSection(timeOfDay = gameState.timeOfDay)

                // 城镇互动节点
                InteractiveNodes(
                    gold = gameState.character.gold,
                    curableCurses = gameState.character.curableCurses,
                    onRest = {
                        val nextTime = when (gameState.timeOfDay) {
                            TimeOfDay.MORNING -> TimeOfDay.AFTERNOON
                            TimeOfDay.AFTERNOON -> TimeOfDay.EVENING
                            TimeOfDay.EVENING -> TimeOfDay.NIGHT
                            TimeOfDay.NIGHT -> TimeOfDay.MORNING
                        }
                        val nextDay = if (gameState.timeOfDay == TimeOfDay.NIGHT) gameState.day + 1 else gameState.day

                        gameState = gameState.copy(
                            character = gameState.character.copy(
                                gold = gameState.character.gold - 15,
                                hp = gameState.character.maxHp,
                                mp = gameState.character.maxMp
                            ),
                            timeOfDay = nextTime,
                            day = nextDay
                        )
                        Toast.makeText(context, "你在炉火旁沉沉睡去，精神完全恢复了...", Toast.LENGTH_SHORT).show()
                    },
                    onBuyElixir = {
                        val healedHp = minOf(gameState.character.hp + 30, gameState.character.maxHp)
                        gameState = gameState.copy(
                            character = gameState.character.copy(
                                gold = gameState.character.gold - 20,
                                hp = healedHp
                            )
                        )
                        Toast.makeText(context, "药水入喉，一股暖流游走遍全身 (HP+30)", Toast.LENGTH_SHORT).show()
                    },
                    onCureCurse = {
                        gameState = gameState.copy(
                            character = gameState.character.copy(
                                gold = gameState.character.gold - 30,
                                curableCurses = maxOf(0, gameState.character.curableCurses - 1)
                            )
                        )
                        Toast.makeText(context, "神圣光辉降下，你感觉灵魂深处的枷锁消失了...", Toast.LENGTH_SHORT).show()
                    }
                )

                // 悬赏任务公告栏
                BountyBoard(
                    quests = gameState.bounties,
                    onAcceptQuest = { id ->
                        gameState = gameState.copy(
                            bounties = gameState.bounties.map {
                                if (it.id == id) it.copy(isAccepted = true) else it
                            }
                        )
                        Toast.makeText(context, "你揭下了悬赏，羊皮纸已被妥善收纳...", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 出征大按钮
                EmbarkButton(
                    onClick = {
                        Toast.makeText(context, "正在骰子检定... 正在推入荒野副本...", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
