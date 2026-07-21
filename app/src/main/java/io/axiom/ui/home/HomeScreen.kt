package io.axiom.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.axiom.data.model.AppCommand
import io.axiom.data.model.FileItem
import io.axiom.ui.components.AnimatedBackground
import io.axiom.ui.components.CommandBar
import io.axiom.ui.components.RecentFilesWing
import io.axiom.ui.components.ResultsPanel
import io.axiom.ui.components.WingSide
import io.axiom.ui.theme.AxiomFileModeColor
import io.axiom.ui.theme.AxiomInk
import io.axiom.ui.theme.AxiomTextDisabled
import io.axiom.ui.theme.AxiomTextPrimary
import io.axiom.ui.theme.AxiomTextSecondary
import kotlinx.coroutines.delay

/**
 * The Axiom home screen — the "Command Stage" layout.
 *
 * Full-screen composition:
 * ```
 * ┌──────────────────────────────────────────┐  ← status bar
 * │                                          │
 * │          [ Greeting text ]               │  ← hero area (fades on focus)
 * │                                          │
 * │  [.kt] [.ts]  [ 🔍  Search… ]  [.py] [.md] │  ← command stage
 * │                                          │
 * │       [ > commands  #symbols ]           │  ← mode hints (fades on focus)
 * │                                          │
 * ├──────────────────────────────────────────┤  ← results panel slides up
 * │  FILES                                   │
 * │  ┌─ MainActivity.kt  ──────────── Kotlin ┐│
 * │  └─ HomeScreen.kt    ──────────── Kotlin ┘│
 * │  COMMANDS                                │
 * │  ┌─ New File  ──────────────────── ⌘N   ┐│
 * └──────────────────────────────────────────┘  ← nav bar
 * ```
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AxiomInk)
    ) {
        val screenHeight = maxHeight

        // ── Layer 1: Animated deep-space background ───────────────────────────
        AnimatedBackground(commandMode = uiState.commandMode)

        // ── Layer 2: Main content ─────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.weight(1.2f))

            // ── Hero greeting ─────────────────────────────────────────────────
            HeroGreeting(
                text      = uiState.greetingText,
                isVisible = !uiState.isCommandBarFocused && uiState.query.isEmpty()
            )

            Spacer(Modifier.weight(0.6f))

            // ── Command stage: wings + bar ────────────────────────────────────
            CommandStage(
                uiState       = uiState,
                onQueryChange = viewModel::onQueryChange,
                onFocusChange = viewModel::onCommandBarFocusChange,
                onClear       = viewModel::onClearQuery,
                onFileClick   = { /* open file */ }
            )

            Spacer(Modifier.weight(0.6f))

            // ── Mode hint row (> commands · # symbols) ────────────────────────
            ModeHints(isVisible = !uiState.isCommandBarFocused && uiState.query.isEmpty())

            Spacer(Modifier.weight(1.4f))
        }

        // ── Layer 3: Results panel (overlays from bottom) ─────────────────────
        ResultsPanel(
            groupedResults = uiState.groupedResults,
            commandMode    = uiState.commandMode,
            isSearching    = uiState.isSearching,
            showEmptyState = uiState.showEmptyState,
            visible        = uiState.isCommandBarFocused &&
                             (uiState.query.isNotEmpty() || !uiState.groupedResults.isEmpty),
            onFileClick    = { /* open file */ },
            onCommandClick = { /* execute command */ },
            modifier       = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(screenHeight * 0.62f)
        )
    }
}

// ── Hero Greeting ─────────────────────────────────────────────────────────────

@Composable
private fun HeroGreeting(
    text: String,
    isVisible: Boolean
) {
    // Entrance: slides down from slightly above and fades in on first launch
    var hasEnteredOnce by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        hasEnteredOnce = true
    }

    val alpha by animateFloatAsState(
        targetValue   = if (isVisible && hasEnteredOnce) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "greeting-alpha"
    )
    val translateY by animateFloatAsState(
        targetValue   = if (isVisible && hasEnteredOnce) 0f else -18f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "greeting-y"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .graphicsLayer { this.alpha = alpha; translationY = translateY }
            .padding(horizontal = 32.dp)
    ) {
        // Subtle pill label above the headline
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            AxiomFileModeColor.copy(alpha = 0.20f),
                            AxiomFileModeColor.copy(alpha = 0.10f)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Text(
                text  = "AXIOM EDITOR",
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = AxiomFileModeColor.copy(alpha = 0.9f),
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize      = 10.sp
                )
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text      = text,
            style     = MaterialTheme.typography.headlineMedium.copy(
                color      = AxiomTextPrimary,
                fontWeight = FontWeight.Bold,
                lineHeight = 38.sp,
                textAlign  = TextAlign.Center
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = "Type to search · > for commands · # for symbols",
            style     = MaterialTheme.typography.bodySmall.copy(
                color      = AxiomTextSecondary.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontSize   = 11.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

// ── Command Stage ─────────────────────────────────────────────────────────────

private val COMPACT_BAR_FRACTION = 0.56f  // fraction of total width in idle state
private val WING_FRACTION        = 0.20f  // each wing's fraction in idle state

@Composable
private fun CommandStage(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onClear: () -> Unit,
    onFileClick: (FileItem) -> Unit
) {
    val isExpanded = uiState.isCommandBarFocused

    // Animate the relative weight of each wing (0 when expanded, WING_FRACTION when idle)
    val wingWeightFraction by animateFloatAsState(
        targetValue   = if (isExpanded) 0.001f else WING_FRACTION,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "wing-weight"
    )
    val wingAlpha by animateFloatAsState(
        targetValue   = if (isExpanded) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "wing-alpha"
    )

    // Clamp animated weight to always be > 0; a bouncy spring can transiently
    // overshoot past zero, which would cause Modifier.weight() to crash.
    val safeWingWeight  = wingWeightFraction.coerceAtLeast(0.0001f)
    val safeCenterWeight = (1f - safeWingWeight * 2).coerceAtLeast(0.0001f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // ── Left wing ─────────────────────────────────────────────────────────
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier         = Modifier
                .weight(safeWingWeight)
                .graphicsLayer { alpha = wingAlpha }
        ) {
            RecentFilesWing(
                files                = uiState.recentFiles.take(3),
                side                 = WingSide.LEFT,
                isCommandBarExpanded = isExpanded,
                onFileClick          = onFileClick
            )
        }

        // ── Command bar ───────────────────────────────────────────────────────
        CommandBar(
            query            = uiState.query,
            commandMode      = uiState.commandMode,
            isExpanded       = isExpanded,
            placeholderIndex = uiState.placeholderIndex,
            isSearching      = uiState.isSearching,
            onQueryChange    = onQueryChange,
            onFocusChange    = onFocusChange,
            onClear          = onClear,
            modifier         = Modifier.weight(safeCenterWeight)
        )

        // ── Right wing ────────────────────────────────────────────────────────
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier         = Modifier
                .weight(safeWingWeight)
                .graphicsLayer { alpha = wingAlpha }
        ) {
            RecentFilesWing(
                files                = uiState.recentFiles.drop(3).take(3),
                side                 = WingSide.RIGHT,
                isCommandBarExpanded = isExpanded,
                onFileClick          = onFileClick
            )
        }
    }
}

// ── Mode Hints ────────────────────────────────────────────────────────────────

@Composable
private fun ModeHints(isVisible: Boolean) {
    AnimatedVisibility(
        visible = isVisible,
        enter   = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                  slideInVertically { it / 4 },
        exit    = fadeOut(tween(150)) + slideOutVertically { it / 4 }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            ModeHintChip(prefix = ">", label = "commands")
            Text(
                text  = "  ·  ",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = AxiomTextDisabled
                )
            )
            ModeHintChip(prefix = "#", label = "symbols")
        }
    }
}

@Composable
private fun ModeHintChip(prefix: String, label: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(AxiomTextDisabled.copy(alpha = 0.15f))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text  = prefix,
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = AxiomTextDisabled,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 10.sp
                )
            )
        }
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color    = AxiomTextDisabled,
                fontSize = 10.sp
            )
        )
    }
}
