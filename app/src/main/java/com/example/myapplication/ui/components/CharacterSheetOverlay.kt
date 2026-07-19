package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.CharacterStats
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch

// 👑 引入底层窗口控制与全面屏沉浸式穿透所必需的导入
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

// 模拟背包物品数据结构
data class GameItem(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val count: Int,
    val type: String // "POTION" (药水), "EQUIP" (装备), "MISC" (杂物)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSheetOverlay(
    stats: CharacterStats,
    onDismissRequest: () -> Unit,
    onUseItem: (GameItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // 强制抽屉弹出时直接完全展开（全屏高度）
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // 👑 核心攻克：拿到 ModalBottomSheet 底层的 Dialog 原生窗口
    // 强制将其 Decor 视窗边界扩展至全屏（FitsSystemWindows = false），
    // 从而使黑色半透明遮罩（Scrim）和抽屉底色无死角铺满底部的系统导航栏，彻底解决漏底痛点！
    val view = LocalView.current
    SideEffect {
        (view.parent as? DialogWindowProvider)?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState,
        containerColor = SurfaceElevated,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        // 👑 修正为您的 M3 版本支持的 contentWindowInsets 参数名称
        contentWindowInsets = { WindowInsets(0.dp) }, 
        dragHandle = {
            // 给拖拽条加上 statusBarsPadding()
            // 确保拖拽条和它下方的所有手账内容都会被系统向下避开状态栏/刘海屏，解决重叠冲突！
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 12.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(ExpressiveShapes.Pill)
                    .background(ArcanePrimary.copy(alpha = 0.25f))
            )
        },
        modifier = modifier.fillMaxHeight(0.96f) // 全屏高度
    ) {
        CharacterSheetContent(
            stats = stats,
            onUseItem = onUseItem
        )
    }
}

/**
 * 手账核心标签卡片与排版
 */
@Composable
private fun CharacterSheetContent(
    stats: CharacterStats,
    onUseItem: (GameItem) -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: 属性卡, 1: 行囊背包
    
    val mockItems = remember {
        mutableStateListOf(
            GameItem("i1", "生命药水", "🧪", "粗糙调配的红色液体。服用后立即恢复 30 点生命值。", 3, "POTION"),
            GameItem("i2", "星辰魔药", "🧪", "散发着幽蓝荧光的神秘药水。回复 20 点魔法值。", 1, "POTION"),
            GameItem("i3", "破旧钢剑", "⚔️", "行会制式短剑，饱经沧桑。+5 攻击力修正。", 1, "EQUIP"),
            GameItem("i4", "秘银护符", "📿", "雕刻着古老星象的银质吊坠。+1 幸运值检定。", 1, "EQUIP"),
            GameItem("i5", "破损的古籍", "📖", "书页已经泛黄腐烂，上面隐约能辨认出某种古代净化仪式的残卷。", 1, "MISC")
        )
    }
    
    var selectedItem by remember { mutableStateOf<GameItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 在最内层容器应用 navigationBarsPadding
            // 确保内容和药水使用按钮被安全推到物理导航键/手势条上方
            .navigationBarsPadding() 
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // 头部：手账标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ADVENTURER'S JOURNAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArcanePrimary
                )
                Text(
                    text = "冒险者手账",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 双页签导航（M3 Expressive 风格高亮胶囊 Tab）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ExpressiveShapes.Pill)
                .background(MaterialTheme.colorScheme.background)
                .padding(4.dp)
        ) {
            listOf("📊 属性状态", "🎒 随身行囊").forEachIndexed { index, tabTitle ->
                val isSelected = activeTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(ExpressiveShapes.Pill)
                        .background(if (isSelected) ArcanePrimary else Color.Transparent)
                        .clickable {
                            activeTab = index
                            selectedItem = null // 切换 Tab 时清除物品选中
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) VoidBackground else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 核心内容区
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "tab_transition",
                modifier = Modifier.fillMaxSize()
            ) { targetTab ->
                when (targetTab) {
                    0 -> StatsTab(stats)
                    1 -> BackpackTab(
                        items = mockItems,
                        selectedItem = selectedItem,
                        onSelectItem = { selectedItem = it },
                        onUseItem = { item ->
                            onUseItem(item)
                            if (item.type == "POTION") {
                                val idx = mockItems.indexOfFirst { it.id == item.id }
                                if (idx != -1) {
                                    val cur = mockItems[idx]
                                    if (cur.count > 1) {
                                        mockItems[idx] = cur.copy(count = cur.count - 1)
                                        selectedItem = mockItems[idx]
                                    } else {
                                        mockItems.removeAt(idx)
                                        selectedItem = null
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * 标签页一：跑团角色属性卡
 */
@Composable
private fun StatsTab(stats: CharacterStats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 角色信息汇总
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ExpressiveShapes.Node)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(ExpressiveShapes.Badge)
                    .background(ArcanePrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛡️", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stats.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "称号: ${stats.title} (等级 ${stats.level})",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "核心属性检定 (DND / TRPG)",
            style = MaterialTheme.typography.labelSmall,
            color = EmberOrange,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 跑团四大核心属性卡
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AttributeBox(name = "💪 力量 (STR)", value = "14", desc = "破障、肉搏与负重能力", modifier = Modifier.weight(1f))
            AttributeBox(name = "⚡ 敏捷 (AGI)", value = "16", desc = "身手、闪避与先攻修正", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AttributeBox(name = "🔮 智力 (INT)", value = "12", desc = "法术亲和与古代见闻秘学", modifier = Modifier.weight(1f))
            AttributeBox(name = "🎲 幸运 (LCK)", value = "15", desc = "影响随机事件与判定暴击率", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 天赋特质
        Text(
            text = "被动特质 / 誓言",
            style = MaterialTheme.typography.labelSmall,
            color = EmberOrange,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TraitChip(title = "✨ 星宿庇护", desc = "星宿之主赐福。在黑暗地道中冒险时，判定大失败概率减半。")
        Spacer(modifier = Modifier.height(6.dp))
        TraitChip(title = "⚔️ 白银誓言", desc = "不可对无辜弱小拔剑。对死灵与狼人伤害 +3。")
    }
}

@Composable
private fun AttributeBox(name: String, value: String, desc: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(ExpressiveShapes.Node)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Text(text = name, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 24.sp, color = ArcanePrimary, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 10.sp, color = TextMuted, lineHeight = 14.sp)
    }
}

@Composable
private fun TraitChip(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.ChipInner)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(ExpressiveShapes.Badge)
                .background(EmberOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✦", fontSize = 12.sp, color = EmberOrange, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = desc, fontSize = 11.sp, color = TextMuted, lineHeight = 15.sp)
        }
    }
}

/**
 * 标签页二：背包与装备详情
 */
@Composable
private fun BackpackTab(
    items: List<GameItem>,
    selectedItem: GameItem?,
    onSelectItem: (GameItem) -> Unit,
    onUseItem: (GameItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "行囊空空如也...", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items) { item ->
                        val isSelected = selectedItem?.id == item.id
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(ExpressiveShapes.ChipInner)
                                .background(if (isSelected) ArcanePrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) ArcanePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = ExpressiveShapes.ChipInner
                                )
                                .clickable { onSelectItem(item) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = item.icon, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.name,
                                    fontSize = 10.sp,
                                    color = if (isSelected) ArcanePrimary else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                            
                            if (item.count > 1) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .clip(ExpressiveShapes.Pill)
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(text = "${item.count}", fontSize = 8.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 选中的物品详情检视框
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(ExpressiveShapes.Node)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), ExpressiveShapes.Node)
                .padding(12.dp)
        ) {
            if (selectedItem == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "👈 点击上方物品查看详情并使用", color = TextMuted, fontSize = 12.sp)
                }
            } else {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${selectedItem.icon} ${selectedItem.name}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcanePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedItem.description,
                            fontSize = 11.sp,
                            color = TextPrimary.copy(alpha = 0.8f),
                            lineHeight = 15.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))

                    if (selectedItem.type == "POTION") {
                        Button(
                            onClick = { onUseItem(selectedItem) },
                            shape = ExpressiveShapes.Pill,
                            colors = ButtonDefaults.buttonColors(containerColor = ArcanePrimary, contentColor = VoidBackground)
                        ) {
                            Text("饮用", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    } else if (selectedItem.type == "EQUIP") {
                        Button(
                            onClick = { onUseItem(selectedItem) },
                            shape = ExpressiveShapes.Pill,
                            colors = ButtonDefaults.buttonColors(containerColor = EmberOrange, contentColor = VoidBackground)
                        ) {
                            Text("装备", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(ExpressiveShapes.Pill)
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("剧情道具", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}