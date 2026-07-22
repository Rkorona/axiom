package io.axiom.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.axiom.data.model.FileItem
import io.axiom.ui.theme.AxiomDusk
import io.axiom.ui.theme.AxiomInk
import io.axiom.ui.theme.AxiomMist
import io.axiom.ui.theme.AxiomSlate
import io.axiom.ui.theme.AxiomTextDisabled
import io.axiom.ui.theme.AxiomTextPrimary
import io.axiom.ui.theme.AxiomTextSecondary
import io.axiom.ui.theme.AxiomViolet
import io.axiom.ui.theme.AxiomVoid
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// ── Layout constants ──────────────────────────────────────────────────────────

private val TREE_HANDLE_H   = 20.dp
private val TREE_PEEK_H     = 44.dp
private val TREE_DIVIDER_H  = 1.dp
private val TREE_CORNER     = 20.dp
private val FILE_ITEM_H     = 52.dp
private val DIR_HEADER_H    = 36.dp
private val TREE_V_PADDING  = 12.dp
private val MAX_VISIBLE_H   = 420.dp
private val FLING_THRESHOLD = -600f

/**
 * A draggable bottom sheet that shows the project file tree.
 *
 * Behaviour mirrors [ProjectsBottomSheet]:
 * - Collapsed: only the peek bar is visible above the command bar
 * - Expanded: content-fitted height showing grouped files
 * - Auto-hides when [isCommandBarFocused] is true
 * - Auto-collapses after a file is selected
 *
 * @param files               Flat list of [FileItem]s from the project scan.
 * @param openFile            Currently active file (highlighted in the list).
 * @param isCommandBarFocused True while the command bar is focused (sheet hides).
 * @param bottomPaddingDp     Extra padding below the peek bar (e.g. command bar height).
 * @param onFileClick         Called when the user taps a file row.
 */
@Composable
fun FileTreeSheet(
    files: List<FileItem>,
    openFile: FileItem?,
    isCommandBarFocused: Boolean,
    bottomPaddingDp: Float = 0f,
    onFileClick: (FileItem) -> Unit
) {
    // Navigation bar height in px — BoxWithConstraints receives the full display
    // height (no insets stripped), so collapsedOffset must add this on top of
    // bottomPaddingDp to keep the peek strip above the command bar.
    val navBarPx = WindowInsets.navigationBars.getBottom(LocalDensity.current)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val density       = LocalDensity.current
        val fullHeightPx  = with(density) { maxHeight.toPx() }
        val peekPx        = with(density) { (TREE_HANDLE_H + TREE_PEEK_H + TREE_DIVIDER_H).toPx() }
        val extraPx       = with(density) { bottomPaddingDp.dp.toPx() } + navBarPx

        // Content-fitted expanded height
        val grouped = remember(files) { groupByDirectory(files) }
        val rawContentDp = run {
            val dirHeaders = grouped.keys.size * DIR_HEADER_H.value
            val fileRows   = files.size * FILE_ITEM_H.value
            val padding    = TREE_V_PADDING.value * 2
            dirHeaders + fileRows + padding
        }
        val clampedContentDp = rawContentDp.coerceAtMost(MAX_VISIBLE_H.value).dp
        val maxSheetVisualDp = TREE_HANDLE_H + TREE_PEEK_H + TREE_DIVIDER_H + clampedContentDp + TREE_V_PADDING

        val minOffsetPx      = with(density) { 80.dp.toPx() }                            // near status bar
        val collapsedOffset  = fullHeightPx - peekPx - extraPx
        val expandedOffset   = max(minOffsetPx, fullHeightPx - with(density) { maxSheetVisualDp.toPx() } - extraPx)

        val offsetY   = remember { Animatable(collapsedOffset) }
        val scope     = rememberCoroutineScope()
        var isExpanded by remember { mutableStateOf(false) }

        // Snap anchors
        LaunchedEffect(fullHeightPx, peekPx, extraPx) {
            val target = if (isExpanded) expandedOffset else collapsedOffset
            offsetY.snapTo(target.coerceIn(minOffsetPx, fullHeightPx))
        }

        // Hide off-screen when command bar is focused
        LaunchedEffect(isCommandBarFocused) {
            if (isCommandBarFocused) {
                scope.launch {
                    offsetY.animateTo(
                        fullHeightPx,
                        spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium)
                    )
                }
                isExpanded = false
            } else {
                scope.launch {
                    offsetY.animateTo(
                        collapsedOffset,
                        spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium)
                    )
                }
            }
        }

        val draggableState = rememberDraggableState { delta ->
            scope.launch {
                val next = (offsetY.value + delta).coerceIn(minOffsetPx, fullHeightPx)
                offsetY.snapTo(next)
            }
        }

        fun snapToAnchor(velocity: Float) {
            val midpoint   = (collapsedOffset + expandedOffset) / 2f
            val goExpanded = velocity < FLING_THRESHOLD || offsetY.value < midpoint
            isExpanded     = goExpanded
            scope.launch {
                offsetY.animateTo(
                    if (goExpanded) expandedOffset else collapsedOffset,
                    spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .draggable(
                    state       = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity -> snapToAnchor(velocity) }
                )
        ) {
            // ── Visual card ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation    = 24.dp,
                        shape        = RoundedCornerShape(topStart = TREE_CORNER, topEnd = TREE_CORNER),
                        spotColor    = AxiomViolet.copy(alpha = 0.15f),
                        ambientColor = AxiomViolet.copy(alpha = 0.08f)
                    )
                    .clip(RoundedCornerShape(topStart = TREE_CORNER, topEnd = TREE_CORNER))
                    .background(AxiomVoid)
            ) {
                // Handle pill
                TreeHandle()

                // Peek bar
                TreePeekBar(
                    fileCount  = files.size,
                    openFile   = openFile,
                    isExpanded = isExpanded,
                    onClick    = { snapToAnchor(if (isExpanded) 0f else FLING_THRESHOLD - 1f) }
                )

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TREE_DIVIDER_H)
                        .background(AxiomDusk.copy(alpha = 0.6f))
                )

                // File list content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(clampedContentDp)
                ) {
                    if (files.isEmpty()) {
                        TreeEmptyState()
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(clampedContentDp)
                        ) {
                            grouped.forEach { (dir, dirFiles) ->
                                // Directory header
                                if (grouped.size > 1 || dir.isNotEmpty()) {
                                    item(key = "dir_$dir") {
                                        DirectoryHeader(path = dir)
                                    }
                                }
                                itemsIndexed(dirFiles, key = { _, f -> f.id }) { _, file ->
                                    FileTreeRow(
                                        file      = file,
                                        isActive  = file.id == openFile?.id,
                                        onClick   = {
                                            onFileClick(file)
                                            // Collapse after selection
                                            isExpanded = false
                                            scope.launch {
                                                offsetY.animateTo(
                                                    collapsedOffset,
                                                    spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Top fade mask
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .align(Alignment.TopCenter)
                                .background(Brush.verticalGradient(listOf(AxiomVoid, Color.Transparent)))
                        )
                    }

                    // Navigation bar spacer inside content
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    )
                }
            }
        }
    }
}

// ── Grouped file map ──────────────────────────────────────────────────────────

/** Groups files by their [FileItem.path] directory. Preserves insertion order. */
private fun groupByDirectory(files: List<FileItem>): Map<String, List<FileItem>> =
    files.groupByTo(LinkedHashMap()) { it.path.trimEnd('/') }

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun TreeHandle() {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(CircleShape)
                .background(AxiomMist.copy(alpha = 0.55f))
        )
    }
}

@Composable
private fun TreePeekBar(
    fileCount: Int,
    openFile: FileItem?,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier              = Modifier
            .fillMaxWidth()
            .height(TREE_PEEK_H)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp)
    ) {
        Icon(
            imageVector        = Icons.Rounded.FolderOpen,
            contentDescription = null,
            tint               = AxiomViolet.copy(alpha = 0.8f),
            modifier           = Modifier.size(18.dp)
        )

        Text(
            text  = "FILES",
            style = MaterialTheme.typography.labelSmall.copy(
                color         = AxiomTextSecondary,
                fontFamily    = FontFamily.Monospace,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.8.sp,
                fontSize      = 10.sp
            )
        )

        // Active file name (if one is open)
        openFile?.let {
            Text(
                text     = "— ${it.name}",
                style    = MaterialTheme.typography.labelSmall.copy(
                    color    = AxiomViolet.copy(alpha = 0.7f),
                    fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        } ?: Spacer(Modifier.weight(1f))

        // Count badge
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AxiomSlate)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text  = fileCount.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = AxiomTextDisabled,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 10.sp
                )
            )
        }

        Spacer(Modifier.width(4.dp))

        Icon(
            imageVector        = Icons.Rounded.KeyboardArrowUp,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint               = AxiomTextDisabled,
            modifier           = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun DirectoryHeader(path: String) {
    val displayPath = path.ifEmpty { "root" }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier
            .fillMaxWidth()
            .height(DIR_HEADER_H)
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text     = displayPath,
            style    = MaterialTheme.typography.labelSmall.copy(
                color         = AxiomTextDisabled,
                fontFamily    = FontFamily.Monospace,
                fontSize      = 10.sp,
                letterSpacing = 0.5.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FileTreeRow(
    file: FileItem,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isActive) AxiomViolet.copy(alpha = 0.10f) else Color.Transparent
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier              = Modifier
            .fillMaxWidth()
            .height(FILE_ITEM_H)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp)
    ) {
        // Language colour dot
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(file.language.colorHex))
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = file.name,
                style    = MaterialTheme.typography.bodyMedium.copy(
                    color      = if (isActive) AxiomViolet else AxiomTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize   = 13.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (file.size > 0L) {
                Text(
                    text  = formatSize(file.size),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color    = AxiomTextDisabled,
                        fontSize = 10.sp
                    )
                )
            }
        }

        // Active indicator bar
        if (isActive) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(22.dp)
                    .clip(CircleShape)
                    .background(AxiomViolet)
            )
        }
    }
}

@Composable
private fun TreeEmptyState() {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Text(
            text  = "No files found",
            style = MaterialTheme.typography.bodySmall.copy(
                color      = AxiomTextDisabled,
                fontFamily = FontFamily.Monospace
            )
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatSize(bytes: Long): String = when {
    bytes < 1_024L        -> "${bytes}B"
    bytes < 1_048_576L    -> "${bytes / 1_024L}KB"
    else                  -> "${"%.1f".format(bytes / 1_048_576.0)}MB"
}
