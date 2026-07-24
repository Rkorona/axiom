package io.axiom.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import io.axiom.ui.theme.AxiomSymbolModeColor
import io.axiom.ui.theme.AxiomTextDisabled
import io.axiom.ui.theme.AxiomTextPrimary
import io.axiom.ui.theme.AxiomVoid

/**
 * 遵循 Material 3 Expressive 规范的悬浮 CommandBar 变形胶囊。
 *
 * 动效增强：
 * 1. 采用 Expressive 物理弹簧控制 Corner Radius、Elevation 与 Scale。
 * 2. 支持 "The Chute" 对接效果：当结果面板在其上方展开时，顶部圆角自动坍缩为 `6.dp` 贴合。
 * 3. 动态模式呼吸高光圈 (Multi-layer Halo Glow)。
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
    /** When true, show a symbol-bar toggle icon to the left of the folder icon.
     *  Should only be true while the keyboard is visible in the editor screen. */
    showSymbolIcon: Boolean = false,
    /** Called when the user taps the symbol-bar icon.  Ignored if [showSymbolIcon] is false. */
    onShowSymbols: (() -> Unit)? = null,
    isConnectedToPanelAbove: Boolean = false,
    fileAccentColor: Color = AxiomFileModeColor,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // ── 模式主色调过渡 ───────────────────────────────────────────────────────
    val modeAccentColor by animateColorAsState(
        targetValue   = when (commandMode) {
            CommandMode.FILE    -> fileAccentColor
            CommandMode.COMMAND -> AxiomCommandModeColor
            CommandMode.SYMBOL  -> AxiomSymbolModeColor
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "cmd-bar-accent"
    )

    // ── 物理圆角演变 ──────────────────────────────────────────────────────────
    val bottomCornerRadius by animateDpAsState(
        targetValue   = if (isExpanded) 20.dp else 30.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "cmd-bar-bottom-corner"
    )

    val topCornerRadius by animateDpAsState(
        targetValue   = if (isExpanded && isConnectedToPanelAbove) 6.dp
                        else if (isExpanded) 20.dp
                        else 30.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "cmd-bar-top-corner"
    )

    // ── 阴影与光晕强弱 ────────────────────────────────────────────────────────
    val elevation by animateDpAsState(
        targetValue   = if (isExpanded) 24.dp else 6.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "cmd-bar-elevation"
    )

    val glowAlpha by animateFloatAsState(
        targetValue   = if (isExpanded) 0.55f else 0.18f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "cmd-bar-glow"
    )

    val barHeight by animateDpAsState(
        targetValue   = if (isExpanded) 58.dp else 52.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "cmd-bar-height"
    )

    val borderAlpha by animateFloatAsState(
        targetValue   = if (isExpanded) 0.75f else 0.25f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "cmd-bar-border"
    )

    val shape = RoundedCornerShape(
        topStart    = topCornerRadius,
        topEnd      = topCornerRadius,
        bottomStart = bottomCornerRadius,
        bottomEnd   = bottomCornerRadius
    )

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
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChange(it.isFocused) }
            .shadow(
                elevation    = elevation,
                shape        = shape,
                spotColor    = modeAccentColor.copy(alpha = 0.55f),
                ambientColor = modeAccentColor.copy(alpha = 0.25f)
            )
            .drawBehind {
                val cr = bottomCornerRadius.toPx()
                drawRoundRect(
                    brush        = Brush.verticalGradient(
                        colors = listOf(
                            modeAccentColor.copy(alpha = glowAlpha * 0.7f),
                            modeAccentColor.copy(alpha = glowAlpha * 0.12f)
                        )
                    ),
                    topLeft      = Offset(-10f, -10f),
                    size         = Size(size.width + 20f, size.height + 20f),
                    cornerRadius = CornerRadius(cr + 10f, cr + 10f)
                )
            }
            .clip(shape)
            .background(AxiomVoid)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        modeAccentColor.copy(alpha = borderAlpha),
                        AxiomMist.copy(alpha = borderAlpha * 0.4f),
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
                // Symbol-bar toggle — only while keyboard is visible in editor
                if (showSymbolIcon && onShowSymbols != null) {
                    IconButton(
                        onClick  = onShowSymbols,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.Terminal,
                            contentDescription = "Switch to symbol bar",
                            tint               = modeAccentColor.copy(alpha = 0.85f),
                            modifier           = Modifier.size(17.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(AxiomMist.copy(alpha = 0.3f))
                    )
                    Spacer(Modifier.width(4.dp))
                }

                if (onFileTreeClick != null) {
                    IconButton(
                        onClick  = onFileTreeClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.FolderOpen,
                            contentDescription = "Browse files",
                            tint               = modeAccentColor.copy(alpha = 0.85f),
                            modifier           = Modifier.size(17.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(AxiomMist.copy(alpha = 0.3f))
                    )
                    Spacer(Modifier.width(8.dp))
                }

                CommandModeIndicator(
                    mode        = commandMode,
                    accentColor = modeAccentColor
                )

                Spacer(Modifier.width(10.dp))

                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier         = Modifier.weight(1f)
                ) {
                    if (query.isEmpty()) {
                        AnimatedContent(
                            targetState    = placeholderIndex,
                            transitionSpec = {
                                (slideInVertically { it / 2 } + fadeIn(tween(220))) togetherWith
                                (slideOutVertically { -it / 2 } + fadeOut(tween(160)))
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
                    innerTextField()
                }

                Spacer(Modifier.width(8.dp))

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

@Composable
private fun CommandModeIndicator(
    mode: CommandMode,
    accentColor: Color
) {
    AnimatedContent(
        targetState    = mode,
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

