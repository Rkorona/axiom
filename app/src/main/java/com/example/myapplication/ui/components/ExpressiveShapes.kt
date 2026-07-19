package com.example.myapplication.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * M3 Expressive 形状规范：用大圆角容器取代原先的八角形符文框与卷轴切角。
 * 保留具名的 shape 常量，方便各组件按层级复用，风格统一。
 */
object ExpressiveShapes {
    val Card = RoundedCornerShape(32.dp)       // 顶层大容器：角色卡、叙事卡、悬赏板
    val CardMedium = RoundedCornerShape(28.dp) // 中等容器：叙事区、悬赏板外框
    val Node = RoundedCornerShape(24.dp)       // 互动节点卡片
    val ChipInner = RoundedCornerShape(20.dp)  // 悬赏板内部子任务卡
    val Badge = RoundedCornerShape(20.dp)      // 等级徽章、图标底
    val Pill = RoundedCornerShape(100.dp)      // 胶囊：血条/魔条、按钮、标签
}
