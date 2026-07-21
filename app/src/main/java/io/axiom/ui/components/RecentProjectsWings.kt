package io.axiom.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.axiom.data.model.Project
import kotlinx.coroutines.delay

/**
 * A vertical stack of [ProjectWingChip]s anchored to one side of the Command Bar.
 *
 * Mirrors [RecentFilesWing] exactly — same staggered spring entrance / exit logic
 * — but displays [Project] chips instead of file chips. Used on the home screen
 * when no project is open.
 *
 * @param projects             Recent projects to show as chips.
 * @param side                 LEFT or RIGHT — controls slide direction.
 * @param isCommandBarExpanded When true, chips animate off-screen.
 * @param onProjectClick       Callback when a chip is tapped.
 * @param maxChips             Maximum number of chips per wing (default 3).
 */
@Composable
fun RecentProjectsWing(
    projects: List<Project>,
    side: WingSide,
    isCommandBarExpanded: Boolean,
    onProjectClick: (Project) -> Unit,
    maxChips: Int = 3,
    modifier: Modifier = Modifier
) {
    val displayProjects = projects.take(maxChips)

    Column(
        horizontalAlignment = if (side == WingSide.LEFT) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier            = modifier
    ) {
        displayProjects.forEachIndexed { index, project ->
            ProjectWingChipSlot(
                project              = project,
                side                 = side,
                isCommandBarExpanded = isCommandBarExpanded,
                entranceDelayMs      = index * 60L,
                onProjectClick       = onProjectClick
            )
        }
    }
}

// ── Private internals ─────────────────────────────────────────────────────────

@Composable
private fun ProjectWingChipSlot(
    project: Project,
    side: WingSide,
    isCommandBarExpanded: Boolean,
    entranceDelayMs: Long,
    onProjectClick: (Project) -> Unit
) {
    var appearedYet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(entranceDelayMs)
        appearedYet = true
    }

    val shouldShow = appearedYet && !isCommandBarExpanded

    val alpha by animateFloatAsState(
        targetValue   = if (shouldShow) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "proj-wing-alpha-${project.id}"
    )

    val slideX by animateDpAsState(
        targetValue   = when {
            !appearedYet         -> if (side == WingSide.LEFT) (-24).dp else 24.dp
            isCommandBarExpanded -> if (side == WingSide.LEFT) (-16).dp else 16.dp
            else                 -> 0.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "proj-wing-x-${project.id}"
    )

    val slideY by animateDpAsState(
        targetValue   = if (shouldShow) 0.dp else (-4).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "proj-wing-y-${project.id}"
    )

    ProjectWingChip(
        project  = project,
        onClick  = { onProjectClick(project) },
        modifier = Modifier
            .widthIn(max = 116.dp)
            .graphicsLayer {
                this.alpha   = alpha
                translationX = slideX.toPx()
                translationY = slideY.toPx()
            }
    )
}
