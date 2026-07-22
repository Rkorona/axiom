package io.axiom.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.axiom.data.model.CommandMode
import io.axiom.ui.home.commandBarHints
import io.axiom.ui.theme.AxiomCommandModeColor
import io.axiom.ui.theme.AxiomFileModeColor
import io.axiom.ui.theme.AxiomMist
import io.axiom.ui.theme.AxiomSlate
import io.axiom.ui.theme.AxiomSymbolModeColor
import io.axiom.ui.theme.AxiomTextDisabled
import io.axiom.ui.theme.AxiomTextPrimary
import io.axiom.ui.theme.AxiomViolet
import io.axiom.ui.theme.AxiomVoid

/**
 * The Command Bar — the centrepiece of the Axiom home screen.
 *
 * Design: a morphing pill that expands into a full-width search field when
 * focused, with spring-physics driven shape, shadow, and colour transitions.
 *
 * States:
 * - **Idle** — compact pill, soft glow, cycling placeholder hints
 * - **Focused (FILE mode)** — expands, violet accent border + glow
 * - **Focused (COMMAND >)** — coral accent, monospace `>` prefix
 * - **Focused (SYMBOL #)** — mint accent, monospace `#` prefix
 *
 * @param query              Current text in the field.
 * @param commandMode        Derived from query prefix; controls accent colour.
 * @param isExpanded         True when the field has keyboard focus.
 * @param placeholderIndex   Index into [commandBarHints] for the cycling hint.
 * @param isSearching        Shows a loading indicator while results stream in.
 * @param onQueryChange      Called on every keystroke.
 * @param onFocusChange      Called when focus state changes.
 * @param hints              Cycling placeholder strings. Defaults to [commandBarHints].
 * @param onClear            Called when the ✕ button is pressed.
 * @param onFileTreeClick    When non-null, shows a folder icon button on the left that
 *                           opens the file tree. Pass null (default) to hide the button
 *                           (e.g. on the home screen where there is no project open).
 */
@Composable
fun CommandBar(
    query: String,
    commandMode: CommandMode,
    isExpanded: Boolean,
    placeholderIndex: Int,
    isSearching: Boolean,
    hints: List<String> = commandBarHints,
    onQueryChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onClear: () -> Unit,
    onFileTreeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // ── Accent colour — smoothly transitions between modes ───────────────────
    val modeAccentColor by animateColorAsState(
        targetValue   = commandMode.toAccentColor(),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "cmd-bar-accent"
    )

    // ── Shape ─────────────────────────────────────────────────────────────────
    val cornerRadius by animateDpAsState(
        targetValue   = if (isExpanded) 20.dp else 30.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "cmd-bar-corner"
    )

    // ── Elevation ─────────────────────────────────────────────────────────────
    val elevation by animateDpAsState(
        targetValue   = if (isExpanded) 20.dp else 6.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "cmd-bar-elevation"
    )

    // ── Glow halo intensity ───────────────────────────────────────────────────
    val glowAlpha by animateFloatAsState(
        targetValue   = if (isExpanded) 0.50f else 0.20f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "cmd-bar-glow"
    )

    // ── Bar height ────────────────────────────────────────────────────────────
    val barHeight by animateDpAsState(
        targetValue   = if (isExpanded) 58.dp else 52.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "cmd-bar-height"
    )

    // ── Border alpha ──────────────────────────────────────────────────────────
    val borderAlpha by animateFloatAsState(
        targetValue   = if (isExpanded) 0.65f else 0.22f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "cmd-bar-border"
    )

    // ── Scale entrance (slight pop when first mounted) ────────────────────────
    val scaleEntrance by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "cmd-bar-entry-scale"
    )

    val shape = RoundedCornerShape(cornerRadius)

    BasicTextField(
        value           = query,
        onValueChange   = onQueryChange,
        singleLine      = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        textStyle       = MaterialTheme.typography.bodyLarge.copy(
            color        = AxiomTextPrimary,
            fontFamily   = FontFamily.Monospace,
            fontWeight   = FontWeight.Normal,
            fontSize     = 15.sp
        ),
        cursorBrush = SolidColor(modeAccentColor),
        modifier    = modifier
            .graphicsLayer { scaleX = scaleEntrance; scaleY = scaleEntrance }
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChange(it.isFocused) }
            .shadow(
                elevation    = elevation,
                shape        = shape,
                spotColor    = modeAccentColor.copy(alpha = 0.50f),
                ambientColor = modeAccentColor.copy(alpha = 0.25f)
            )
            // Outer glow halo — drawn behind the clipped surface
            .drawBehind {
                val cr = cornerRadius.toPx()
                drawRoundRect(
                    brush        = Brush.verticalGradient(
                        colors = listOf(
                            modeAccentColor.copy(alpha = glowAlpha * 0.6f),
                            modeAccentColor.copy(alpha = glowAlpha * 0.15f)
                        )
                    ),
                    topLeft      = Offset(-12f, -12f),
                    size         = Size(size.width + 24f, size.height + 24f),
                    cornerRadius = CornerRadius(cr + 12f, cr + 12f)
                )
            }
            .clip(shape)
            .background(AxiomVoid)
            // Accent border ring
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        modeAccentColor.copy(alpha = borderAlpha),
                        AxiomMist.copy(alpha = borderAlpha * 0.5f),
                        modeAccentColor.copy(alpha = borderAlpha * 0.8f)
                    )
                ),
                shape = shape
            ),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .padding(horizontal = 18.dp)
            ) {
                // ── Folder button (editor only — hidden on home screen) ────────
                if (onFileTreeClick != null) {
                    IconButton(
                        onClick  = onFileTreeClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.FolderOpen,
                            contentDescription = "Browse files",
                            tint               = AxiomViolet.copy(alpha = 0.75f),
                            modifier           = Modifier.size(17.dp)
                        )
                    }
                    // Thin vertical divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(AxiomMist.copy(alpha = 0.25f))
                    )
                    Spacer(Modifier.width(8.dp))
                }

                // ── Mode indicator ────────────────────────────────────────────
                CommandModeIndicator(
                    mode        = commandMode,
                    accentColor = modeAccentColor
                )

                Spacer(Modifier.width(10.dp))

                // ── Text field + animated placeholder (centre) ────────────────
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier         = Modifier.weight(1f)
                ) {
                    // Cycling placeholder — only visible when query is empty
                    if (query.isEmpty()) {
                        AnimatedContent(
                            targetState   = placeholderIndex,
                            transitionSpec = {
                                (slideInVertically { it / 3 } + fadeIn(tween(220))) togetherWith
                                (slideOutVertically { -it / 3 } + fadeOut(tween(160)))
                            },
                            label = "placeholder-cycle"
                        ) { index ->
                            Text(
                                text  = hints.getOrElse(index) { hints.getOrNull(0) ?: "" },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color      = AxiomTextDisabled,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize   = 14.sp
                                )
                            )
                        }
                    }
                    // The actual text input
                    innerTextField()
                }

                Spacer(Modifier.width(8.dp))

                // ── Loading spinner ───────────────────────────────────────────
                AnimatedVisibility(
                    visible = isSearching,
                    enter   = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit    = scaleOut() + fadeOut()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            color       = modeAccentColor,
                            strokeWidth = 2.dp,
                            trackColor  = modeAccentColor.copy(alpha = 0.15f)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                }

                // ── Clear button ──────────────────────────────────────────────
                AnimatedVisibility(
                    visible = query.isNotEmpty() && !isSearching,
                    enter   = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit    = scaleOut() + fadeOut()
                ) {
                    IconButton(
                        onClick  = {
                            onClear()
                            focusRequester.requestFocus()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.Close,
                            contentDescription = "Clear query",
                            tint               = AxiomMist,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    )
}

// ── Mode indicator composable ─────────────────────────────────────────────────

/**
 * The left-side indicator inside the Command Bar.
 *
 * - FILE mode    → magnifying-glass icon (animates in from the left)
 * - COMMAND mode → `>` text prefix
 * - SYMBOL mode  → `#` text prefix
 *
 * Transitions between modes with a vertical slide + fade.
 */
@Composable
private fun CommandModeIndicator(
    mode: CommandMode,
    accentColor: Color
) {
    AnimatedContent(
        targetState   = mode,
        transitionSpec = {
            (slideInVertically { -it } + fadeIn(tween(180))) togetherWith
            (slideOutVertically {  it } + fadeOut(tween(120)))
        },
        label = "mode-indicator"
    ) { currentMode ->
        when (currentMode) {
            CommandMode.FILE -> Icon(
                imageVector        = Icons.Rounded.Search,
                contentDescription = "Search files",
                tint               = accentColor,
                modifier           = Modifier.size(20.dp)
            )
            CommandMode.COMMAND -> Text(
                text  = ">",
                style = MaterialTheme.typography.labelLarge.copy(
                    color      = accentColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
            )
            CommandMode.SYMBOL -> Text(
                text  = "#",
                style = MaterialTheme.typography.labelLarge.copy(
                    color      = accentColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
            )
        }
    }
}

// ── Extension helpers ─────────────────────────────────────────────────────────

private fun CommandMode.toAccentColor(): Color = when (this) {
    CommandMode.FILE    -> AxiomFileModeColor
    CommandMode.COMMAND -> AxiomCommandModeColor
    CommandMode.SYMBOL  -> AxiomSymbolModeColor
}
