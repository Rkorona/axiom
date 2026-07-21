package io.axiom.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.axiom.data.model.Project
import io.axiom.ui.theme.AxiomFileModeColor
import io.axiom.ui.theme.AxiomTextSecondary
import io.axiom.ui.theme.AxiomVoid
import kotlinx.coroutines.delay

/**
 * The idle bottom area of the Home screen.
 *
 * Shown when no search is active. Displays a RECENT section header followed
 * by a staggered list of [ProjectCard]s. When the list is empty, a friendly
 * empty state hints at the "> New Project" command.
 *
 * Shares the same slide-in/out animation system as [ResultsPanel] so the two
 * panels hand off smoothly without any overlap.
 *
 * @param projects       Recent projects from the database.
 * @param visible        Drives the AnimatedVisibility container.
 * @param onProjectClick Callback when a project card is tapped.
 * @param onNewProject   Called when the user taps the empty-state CTA.
 */
@Composable
fun ProjectsPanel(
    projects: List<Project>,
    visible: Boolean,
    onProjectClick: (Project) -> Unit,
    onNewProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter   = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMediumLow
            ),
            initialOffsetY = { it / 2 }
        ) + fadeIn(tween(200)),
        exit    = slideOutVertically(
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
            targetOffsetY = { it / 3 }
        ) + fadeOut(tween(180)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(AxiomVoid)
        ) {
            if (projects.isEmpty()) {
                ProjectsEmptyState(onNewProject = onNewProject)
            } else {
                ProjectsList(
                    projects       = projects,
                    onProjectClick = onProjectClick
                )
            }
        }
    }
}

// ── Projects list ─────────────────────────────────────────────────────────────

@Composable
private fun ProjectsList(
    projects: List<Project>,
    onProjectClick: (Project) -> Unit
) {
    // Stagger-reveal items as they load
    val revealedItems = remember(projects) { mutableStateListOf<Int>() }

    LaunchedEffect(projects) {
        revealedItems.clear()
        // Header + each project card
        val totalItems = 1 + projects.size
        repeat(totalItems) { index ->
            delay(30L + index * 40L)
            revealedItems.add(index)
        }
    }

    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier            = Modifier.fillMaxSize()
    ) {
        // Section header
        item(key = "header-recent") {
            val headerRevealed = 0 in revealedItems
            AnimatedVisibility(
                visible = headerRevealed,
                enter   = fadeIn(spring(stiffness = Spring.StiffnessMedium))
            ) {
                SectionHeader(
                    title       = "RECENT",
                    count       = projects.size,
                    accentColor = AxiomFileModeColor,
                    modifier    = Modifier.fillMaxWidth()
                )
            }
        }

        // Project cards with staggered entrance
        itemsIndexed(projects, key = { _, p -> "project-${p.id}" }) { index, project ->
            val cardRevealed = (index + 1) in revealedItems  // +1 for header
            AnimatedVisibility(
                visible = cardRevealed,
                enter   = slideInVertically(
                    animationSpec  = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMediumLow
                    ),
                    initialOffsetY = { it / 2 }
                ) + fadeIn(spring(stiffness = Spring.StiffnessMedium))
            ) {
                ProjectCard(
                    project = project,
                    onClick = { onProjectClick(project) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun ProjectsEmptyState(onNewProject: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier            = Modifier
            .fillMaxSize()
            .padding(vertical = 40.dp, horizontal = 32.dp)
    ) {
        Icon(
            imageVector        = Icons.Rounded.FolderOpen,
            contentDescription = null,
            tint               = AxiomTextSecondary.copy(alpha = 0.35f),
            modifier           = Modifier.size(52.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text      = "No projects yet",
            style     = MaterialTheme.typography.titleSmall.copy(
                color = AxiomTextSecondary.copy(alpha = 0.65f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = "Type  > New Project  to create one\nor  > Open Folder  to import",
            style     = MaterialTheme.typography.bodySmall.copy(
                color      = AxiomTextSecondary.copy(alpha = 0.40f),
                fontFamily = FontFamily.Monospace,
                fontSize   = 11.sp,
                lineHeight = 18.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
