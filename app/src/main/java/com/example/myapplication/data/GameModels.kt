package com.example.myapplication.data

/**
 * 跑团游戏中的时间流逝阶段
 */
enum class TimeOfDay(val label: String, val description: String) {
    MORNING("清晨", "薄雾笼罩着风暴斗篷酒馆。门外的集市还没开张，空气中弥漫着清凉的水汽与松木香。"),
    AFTERNOON("正午", "酒馆里嘈杂起来。商贩、佣兵和异乡旅客高谈阔论，清脆的木杯碰撞声不绝于耳。"),
    EVENING("黄昏", "落日余晖被森林边缘的迷雾吞噬。酒馆燃起了松脂火把，冒险者们开始低声交换传闻。"),
    NIGHT("深夜", "炉火微弱地跳动着。角落里坐着沉默的兜帽怪客。窗外野兽的低嚎让木屋显得格外安全。")
}

/**
 * 角色核心卡片属性
 */
data class CharacterStats(
    val name: String,
    val title: String,
    val hp: Int,
    val maxHp: Int,
    val mp: Int,
    val maxMp: Int,
    val gold: Int,
    val level: Int,
    val curableCurses: Int = 0
)

/**
 * 悬赏任务
 */
data class BountyQuest(
    val id: String,
    val title: String,
    val difficulty: String, // 简单, 困难, 噩梦
    val reward: Int,
    val description: String,
    val isAccepted: Boolean = false
)

/**
 * 整体主页的状态集
 */
data class GameState(
    val character: CharacterStats,
    val timeOfDay: TimeOfDay,
    val day: Int,
    val bounties: List<BountyQuest>
)
