package io.axiom.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.axiom.data.model.CommandMode
import io.axiom.data.model.FileItem
import io.axiom.data.model.Project
import io.axiom.ui.components.AnimatedBackground
import io.axiom.ui.components.CommandBar
import io.axiom.ui.components.FileTreeModalSheet
import io.axiom.ui.components.ResultsPanel
import io.axiom.ui.theme.AxiomDusk
import io.axiom.ui.theme.AxiomInk
import io.axiom.ui.theme.AxiomMist
import io.axiom.ui.theme.AxiomTextDisabled
import io.axiom.ui.theme.AxiomTextPrimary
import io.axiom.ui.theme.AxiomTextSecondary
import io.axiom.ui.theme.AxiomViolet

/**
 * The Axiom editor screen — state ③ of the home state machine.
 *
 * Layout:
 * ```
 * ┌─ Box (full screen) ──────────────────────────────────────────┐
 * │  Layer 1 — AnimatedBackground                                │
 * │  Layer 2 — Column                                            │
 * │    EditorTopBar (project name · file breadcrumb · save)      │
 * │    EditorSurface or EmptyState  (weight 1f)                  │
 * │    ResultsPanel (visible while command bar focused)          │
 * │    CommandBar  ← B2: the primary operation surface           │
 * │    navigationBarsPadding                                     │
 * │  Layer 3 — FileTreeSheet (draggable overlay)                 │
 * └──────────────────────────────────────────────────────────────┘
 * ```
 *
 * @param projectId Room id of the project to open.
 * @param onBack    Called when the user presses the back arrow.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun EditorScreen(
    projectId: Long,
    onBack: () -> Unit,
    viewModel: EditorViewModel = viewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val uiState    by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val focused    = uiState.isCommandBarFocused
    var showFileTree by remember { mutableStateOf(false) }

    // Load project once on first composition
    LaunchedEffect(projectId) { viewModel.loadProject(projectId) }

    // Back press: collapse command bar first; then pop the back stack
    BackHandler(enabled = focused) { focusManager.clearFocus() }

    val scrimAlpha by animateFloatAsState(
        targetValue   = if (focused) 0.35f else 0f,
        animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow),
        label         = "editor-scrim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AxiomInk)
            .pointerInput(focused) {
                if (focused) detectTapGestures { focusManager.clearFocus() }
            }
    ) {
        // ── Layer 1: Background ───────────────────────────────────────────────
        AnimatedBackground(commandMode = uiState.commandMode)

        // ── Layer 2: Scrim ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
        )

        // ── Layer 3: Main content column ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            // Top bar
            EditorTopBar(
                project  = uiState.project,
                openFile = uiState.openFile,
                isDirty  = uiState.isDirty,
                onBack   = onBack,
                onSave   = viewModel::saveCurrentFile
            )

            // Editor surface — Results panel overlays this area when command bar is focused
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    uiState.isLoadingContent -> LoadingOverlay()
                    uiState.openFile != null -> EditorSurface(
                        content         = uiState.fileContent,
                        onContentChange = viewModel::onContentChange
                    )
                    else                     -> EditorEmptyState(isLoadingFiles = uiState.isLoadingFiles)
                }

                // Results panel slides up over the editor surface when searching
                ResultsPanel(
                    groupedResults = uiState.groupedResults,
                    commandMode    = uiState.commandMode,
                    isSearching    = uiState.isSearching,
                    showEmptyState = uiState.showEmptyState,
                    visible        = focused && (uiState.query.isNotEmpty() || !uiState.groupedResults.isEmpty),
                    onFileClick    = viewModel::onFileClick,
                    onCommandClick = viewModel::onCommandClick,
                    modifier       = Modifier.fillMaxSize()
                )
            }

            // Command bar — B2 primary operation surface, always present at bottom
            // Plan C: sharedElement shares this bar with the home-screen CommandBar
            // so it morphs across the transition rather than fading in from nothing.
            val cmdBarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .sharedElement(
                            state                   = rememberSharedContentState(key = "command-bar"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                }
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            }
            CommandBar(
                query            = uiState.query,
                commandMode      = uiState.commandMode,
                isExpanded       = focused,
                placeholderIndex = uiState.placeholderIndex,
                isSearching      = uiState.isSearching,
                hints            = editorCommandBarHints,
                onQueryChange    = viewModel::onQueryChange,
                onFocusChange    = viewModel::onCommandBarFocusChange,
                onClear          = viewModel::onClearQuery,
                onFileTreeClick  = { showFileTree = true },
                modifier         = cmdBarModifier
            )

            Spacer(Modifier.navigationBarsPadding())
        }

        // ── File tree modal ───────────────────────────────────────────────────
        // Opened by the folder icon in CommandBar; replaces the old draggable
        // peek sheet, eliminating the WindowInsets timing race entirely.
        if (showFileTree) {
            FileTreeModalSheet(
                files       = uiState.files,
                openFile    = uiState.openFile,
                onDismiss   = { showFileTree = false },
                onFileClick = viewModel::onFileClick
            )
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun EditorTopBar(
    project: Project?,
    openFile: FileItem?,
    isDirty: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp)
    ) {
        // Back button
        IconButton(onClick = onBack) {
            Icon(
                imageVector        = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back",
                tint               = AxiomTextSecondary,
                modifier           = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(2.dp))

        // Project name + file breadcrumb
        Column(modifier = Modifier.weight(1f)) {
            if (project != null) {
                Text(
                    text     = project.name,
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
            if (openFile != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language colour dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(openFile.language.colorHex))
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text     = openFile.name,
                        style    = MaterialTheme.typography.bodyMedium.copy(
                            color      = AxiomTextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize   = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Unsaved dot
                    if (isDirty) {
                        Spacer(Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(AxiomViolet)
                        )
                    }
                }
            } else {
                Text(
                    text  = "No file open",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color    = AxiomTextDisabled,
                        fontSize = 12.sp
                    )
                )
            }
        }

        // Save button — only visible when there are unsaved changes
        AnimatedVisibility(
            visible = isDirty,
            enter   = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit    = fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            IconButton(onClick = onSave) {
                Icon(
                    imageVector        = Icons.Rounded.Save,
                    contentDescription = "Save",
                    tint               = AxiomViolet,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }

    // Top bar divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AxiomDusk.copy(alpha = 0.5f))
    )
}

// ── Editor surface ────────────────────────────────────────────────────────────

@Composable
private fun EditorSurface(
    content: String,
    onContentChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        BasicTextField(
            value         = content,
            onValueChange = onContentChange,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            textStyle     = TextStyle(
                color        = AxiomTextPrimary,
                fontFamily   = FontFamily.Monospace,
                fontSize     = 13.sp,
                lineHeight   = 22.sp,
                letterSpacing = 0.sp
            ),
            cursorBrush   = SolidColor(AxiomViolet),
            maxLines      = Int.MAX_VALUE,
            decorationBox = { innerTextField -> innerTextField() }
        )
    }
}

// ── Loading / empty states ────────────────────────────────────────────────────

@Composable
private fun LoadingOverlay() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier    = Modifier.size(28.dp),
            color       = AxiomViolet,
            strokeWidth = 2.dp,
            trackColor  = AxiomViolet.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun EditorEmptyState(isLoadingFiles: Boolean) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        if (isLoadingFiles) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(24.dp),
                    color       = AxiomViolet,
                    strokeWidth = 2.dp,
                    trackColor  = AxiomViolet.copy(alpha = 0.15f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text  = "Scanning files…",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color      = AxiomTextDisabled,
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 11.sp
                    )
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text  = "📁",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color    = AxiomMist,
                        fontSize = 30.sp
                    )
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text  = "Tap the folder icon to open a file",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color      = AxiomTextDisabled,
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 12.sp
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text  = "or type a filename in the bar below",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color    = AxiomTextDisabled,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
