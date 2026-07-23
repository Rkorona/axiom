package io.axiom.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.axiom.data.model.Project
import io.axiom.ui.theme.AxiomDusk
import io.axiom.ui.theme.AxiomFileModeColor
import io.axiom.ui.theme.AxiomMist
import io.axiom.ui.theme.AxiomSlate
import io.axiom.ui.theme.AxiomTextDisabled
import io.axiom.ui.theme.AxiomTextSecondary
import io.axiom.ui.theme.AxiomVoid
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val PEEK_BASE_HEIGHT   = 72.dp
private val SHEET_CORNER       = 28.dp

private val SHEET_HANDLE_H     = 20.dp
private val SHEET_PEEK_BAR_H   = 48.dp
private val SHEET_DIVIDER_H    = 1.dp
private val LIST_V_PAD         = 8.dp
private val LIST_HEADER_H      = 40.dp
private val LIST_SPACING       = 8.dp
private val CARD_H             = 68.dp
private val LIST_BOTTOM_SPACER = 16.dp
private val EMPTY_STATE_H      = 200.dp

@Composable
fun ProjectsBottomSheet(
    projects: List<Project>,
    isSearchActive: Boolean,
    onProjectClick: (Project) -> Unit,
    onNewProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density         = LocalDensity.current
    val scope           = rememberCoroutineScope()
    val navBarHeight    = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val peekDp: Dp = PEEK_BASE_HEIGHT + navBarHeight
    val peekPx: Float
    val minOffsetPx: Float
    with(density) {
        peekPx      = peekDp.toPx().coerceAtLeast(1f)
        minOffsetPx = (statusBarHeight + 20.dp).toPx().coerceAtLeast(0f)
    }

    val offsetAnim      = remember { Animatable(20000f) }
    var collapsedOffset by remember { mutableFloatStateOf(0f) }
    var isExpanded      by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val fullHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }

        val peekSectionDp = SHEET_HANDLE_H + SHEET_PEEK_BAR_H +
                navBarHeight.coerceAtLeast(8.dp) + SHEET_DIVIDER_H

        val rawContentDp: Dp = when {
            projects.isEmpty() -> EMPTY_STATE_H
            else -> {
                val n = projects.size
                LIST_V_PAD + LIST_HEADER_H + LIST_SPACING +
                        CARD_H * n + LIST_SPACING * (n - 1) +
                        LIST_BOTTOM_SPACER + LIST_V_PAD
            }
        }

        val maxSheetDp       = with(density) { (fullHeightPx - minOffsetPx).toDp() }
        val clampedContentDp = rawContentDp.coerceAtMost((maxSheetDp - peekSectionDp).coerceAtLeast(0.dp))
        val maxSheetVisualDp = peekSectionDp + clampedContentDp

        val expandedOffset = with(density) {
            (fullHeightPx - maxSheetVisualDp.toPx()).coerceAtLeast(minOffsetPx)
        }

        LaunchedEffect(fullHeightPx, peekPx) {
            collapsedOffset = fullHeightPx - peekPx
            if (!isSearchActive) {
                offsetAnim.snapTo(collapsedOffset)
            }
        }

        LaunchedEffect(isSearchActive) {
            if (collapsedOffset <= 0f) return@LaunchedEffect
            if (isSearchActive) {
                isExpanded = false
                offsetAnim.animateTo(
                    targetValue   = collapsedOffset + peekPx,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness    = Spring.StiffnessMedium
                    )
                )
            } else {
                offsetAnim.animateTo(
                    targetValue   = collapsedOffset,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMediumLow
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = offsetAnim.value.roundToInt()) }
                .draggable(
                    orientation = Orientation.Vertical,
                    state       = rememberDraggableState { delta ->
                        scope.launch {
                            offsetAnim.snapTo(
                                (offsetAnim.value + delta)
                                    .coerceIn(expandedOffset, collapsedOffset + peekPx)
                            )
                        }
                    },
                    onDragStopped = { velocity ->
                        scope.launch {
                            val current = offsetAnim.value
                            val travel  = collapsedOffset - expandedOffset
                            val snapUp  = velocity < -600f ||
                                    (travel > 0f && current < collapsedOffset - travel * 0.55f)
                            val target  = if (snapUp) {
                                isExpanded = true
                                expandedOffset
                            } else {
                                isExpanded = false
                                collapsedOffset
                            }
                            offsetAnim.animateTo(
                                targetValue     = target,
                                animationSpec   = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness    = Spring.StiffnessMediumLow
                                ),
                                initialVelocity = velocity
                            )
                        }
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SHEET_CORNER)
                    .shadow(
                        elevation    = 24.dp,
                        shape        = RoundedCornerShape(topStart = SHEET_CORNER, topEnd = SHEET_CORNER),
                        ambientColor = Color.Black.copy(alpha = 0.4f),
                        spotColor    = Color.Black.copy(alpha = 0.3f)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.Top, unbounded = false)
                    .heightIn(max = maxSheetVisualDp)
                    .clip(RoundedCornerShape(topStart = SHEET_CORNER, topEnd = SHEET_CORNER))
                    .background(AxiomVoid)
            ) {
                SheetHandle()

                SheetPeekBar(
                    projects        = projects,
                    navBarBottomPad = navBarHeight
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SHEET_DIVIDER_H)
                        .background(AxiomDusk.copy(alpha = 0.6f))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(clampedContentDp)
                ) {
                    if (projects.isEmpty()) {
                        ProjectsEmptyState(onNewProject = onNewProject)
                    } else {
                        ProjectsList(
                            projects       = projects,
                            onProjectClick = onProjectClick,
                            listModifier   = Modifier
                                .fillMaxWidth()
                                .height(clampedContentDp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(AxiomVoid, Color.Transparent)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp)
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
private fun SheetPeekBar(
    projects: List<Project>,
    navBarBottomPad: Dp
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = navBarBottomPad.coerceAtLeast(8.dp))
            .height(48.dp)
    ) {
        Icon(
            imageVector        = Icons.Rounded.FolderOpen,
            contentDescription = null,
            tint               = AxiomTextSecondary.copy(alpha = 0.55f),
            modifier           = Modifier.size(17.dp)
        )

        Spacer(Modifier.width(2.dp))

        Text(
            text     = "PROJECTS",
            style    = MaterialTheme.typography.labelSmall.copy(
                color         = AxiomTextSecondary.copy(alpha = 0.6f),
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontFamily    = FontFamily.Monospace,
                fontSize      = 10.sp
            ),
            modifier = Modifier.weight(1f)
        )

        if (projects.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AxiomSlate)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text  = "${projects.size}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color      = AxiomTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 11.sp
                    )
                )
            }
        } else {
            Text(
                text  = "> New Project",
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = AxiomFileModeColor.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 10.sp
                )
            )
        }

        Text(
            text  = "↑",
            style = MaterialTheme.typography.labelSmall.copy(
                color    = AxiomTextDisabled.copy(alpha = 0.45f),
                fontSize = 11.sp
            )
        )
    }
}