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
import androidx.compose.material.icons.rounded.SearchOff
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.axiom.data.model.AppCommand
import io.axiom.data.model.CommandMode
import io.axiom.data.model.FileItem
import io.axiom.data.model.GroupedResults
import io.axiom.data.model.SearchResult
import io.axiom.data.model.SymbolKind
import io.axiom.ui.theme.AxiomCommandModeColor
import io.axiom.ui.theme.AxiomFileModeColor
import io.axiom.ui.theme.AxiomInk
import io.axiom.ui.theme.AxiomSymbolModeColor
import io.axiom.ui.theme.AxiomTextSecondary
import io.axiom.ui.theme.AxiomVoid
import io.axiom.ui.theme.SymbolTextStyle
import kotlinx.coroutines.delay

/**
 * The results panel that slides up from the bottom of the screen when
 * the Command Bar is focused and search results (or an empty state) are ready.
 *
 * Structure:
 * - Fade + slide-in container
 * - Inside: frosted surface with top-fade gradient edge
 * - Sections rendered from [GroupedResults] with [SectionHeader]s
 * - Each item animates in with a staggered spring entrance
 * - Empty state when query has text but no results
 *
 * @param groupedResults Search results grouped by type.
 * @param commandMode    Current mode — controls section accent colours.
 * @param isSearching    Suppresses empty state while debounce is running.
 * @param showEmptyState Shows the "no results" state when true.
 * @param visible        Whether the panel should be on screen.
 */
@Composable
fun ResultsPanel(
    groupedResults: GroupedResults,
    commandMode: CommandMode,
    isSearching: Boolean,
    showEmptyState: Boolean,
    visible: Boolean,
    onFileClick: (FileItem) -> Unit,
    onCommandClick: (AppCommand) -> Unit,
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
            // Fade gradient at the very top of the panel for a "frosted" feel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(AxiomVoid, AxiomVoid.copy(alpha = 0f))
                        )
                    )
                    .align(Alignment.TopCenter)
            )

            if (showEmptyState && !isSearching) {
                EmptyResultsState()
            } else {
                ResultsList(
                    groupedResults = groupedResults,
                    commandMode    = commandMode,
                    onFileClick    = onFileClick,
                    onCommandClick = onCommandClick
                )
            }
        }
    }
}

// ── Results list ──────────────────────────────────────────────────────────────

@Composable
private fun ResultsList(
    groupedResults: GroupedResults,
    commandMode: CommandMode,
    onFileClick: (FileItem) -> Unit,
    onCommandClick: (AppCommand) -> Unit
) {
    // Track which items have completed their entrance animation
    val revealedItems = remember(groupedResults) { mutableStateListOf<Int>() }

    val allItems = buildList {
        if (groupedResults.files.isNotEmpty()) {
            add(SectionMarker("FILES", groupedResults.files.size, AxiomFileModeColor))
            addAll(groupedResults.files)
        }
        if (groupedResults.commands.isNotEmpty()) {
            add(SectionMarker("COMMANDS", groupedResults.commands.size, AxiomCommandModeColor))
            addAll(groupedResults.commands)
        }
        if (groupedResults.symbols.isNotEmpty()) {
            add(SectionMarker("SYMBOLS", groupedResults.symbols.size, AxiomSymbolModeColor))
            addAll(groupedResults.symbols)
        }
    }

    // Stagger-reveal items sequentially
    LaunchedEffect(groupedResults) {
        revealedItems.clear()
        allItems.forEachIndexed { index, _ ->
            delay(30L + index * 28L)
            revealedItems.add(index)
        }
    }

    LazyColumn(
        contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier        = Modifier.fillMaxSize()
    ) {
        itemsIndexed(allItems, key = { i, item ->
            when (item) {
                is SectionMarker             -> "section-${item.title}"
                is SearchResult.FileResult   -> "file-${item.file.id}"
                is SearchResult.CommandResult-> "cmd-${item.command.id}"
                is SearchResult.SymbolResult -> "sym-${item.file.id}-${item.kind.name}-${item.line}-${item.symbol}"
                else                         -> i
            }
        }) { index, item ->
            val revealed = index in revealedItems
            StaggeredRevealItem(revealed = revealed, index = index) {
                when (item) {
                    is SectionMarker -> {
                        SectionHeader(
                            title       = item.title,
                            count       = item.count,
                            accentColor = item.color,
                            modifier    = Modifier.fillMaxWidth()
                        )
                    }
                    is SearchResult.FileResult ->
                        FileResultCard(
                            file      = item.file,
                            onClick   = { onFileClick(item.file) },
                            modifier  = Modifier.fillMaxWidth()
                        )
                    is SearchResult.CommandResult ->
                        CommandResultCard(
                            command   = item.command,
                            onClick   = { onCommandClick(item.command) },
                            modifier  = Modifier.fillMaxWidth()
                        )
                    is SearchResult.SymbolResult ->
                        SymbolResultCard(
                            result    = item,
                            modifier  = Modifier.fillMaxWidth()
                        )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) } // breathing room; nav bar handled by outer Column
    }
}

// ── Staggered reveal wrapper ──────────────────────────────────────────────────

@Composable
private fun StaggeredRevealItem(
    revealed: Boolean,
    index: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = revealed,
        enter   = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMediumLow
            ),
            initialOffsetY = { it / 2 }
        ) + fadeIn(spring(stiffness = Spring.StiffnessMedium))
    ) {
        content()
    }
}

// ── Symbol result card ────────────────────────────────────────────────────────

@Composable
private fun SymbolResultCard(
    result: SearchResult.SymbolResult,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AxiomVoid)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = result.symbol,
                style = SymbolTextStyle.copy(
                    color      = AxiomSymbolModeColor,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 13.sp
                )
            )
            Text(
                text  = "${result.file.name}:${result.line}  ·  ${result.kind.label}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = AxiomTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 10.sp
                )
            )
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyResultsState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier            = Modifier
            .fillMaxSize()
            .padding(vertical = 56.dp)
    ) {
        Icon(
            imageVector        = Icons.Rounded.SearchOff,
            contentDescription = null,
            tint               = AxiomTextSecondary.copy(alpha = 0.4f),
            modifier           = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text      = "No results",
            style     = MaterialTheme.typography.titleSmall.copy(
                color = AxiomTextSecondary.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = "Try a different query or check your spelling",
            style     = MaterialTheme.typography.bodySmall.copy(
                color = AxiomTextSecondary.copy(alpha = 0.4f)
            ),
            textAlign = TextAlign.Center
        )
    }
}

// ── Internal data types ───────────────────────────────────────────────────────

private data class SectionMarker(
    val title: String,
    val count: Int,
    val color: androidx.compose.ui.graphics.Color
)
