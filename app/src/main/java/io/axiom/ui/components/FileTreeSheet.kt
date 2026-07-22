package io.axiom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.axiom.data.model.FileItem
import io.axiom.ui.theme.AxiomDusk
import io.axiom.ui.theme.AxiomMist
import io.axiom.ui.theme.AxiomSlate
import io.axiom.ui.theme.AxiomTextDisabled
import io.axiom.ui.theme.AxiomTextPrimary
import io.axiom.ui.theme.AxiomTextSecondary
import io.axiom.ui.theme.AxiomViolet
import io.axiom.ui.theme.AxiomVoid

// ── Layout constants ──────────────────────────────────────────────────────────

private val FILE_ITEM_H    = 52.dp
private val DIR_HEADER_H   = 36.dp
private val TREE_V_PADDING = 12.dp
private val MAX_VISIBLE_H  = 480.dp

/**
 * A standard [ModalBottomSheet] that shows the project file tree.
 *
 * Replaces the previous custom draggable peek-sheet to eliminate the
 * WindowInsets timing race and the layout conflict with the CommandBar.
 *
 * @param files       Flat list of [FileItem]s from the project scan.
 * @param openFile    Currently active file (highlighted in the list).
 * @param onDismiss   Called when the sheet is dismissed (back press, drag, scrim tap).
 * @param onFileClick Called when the user taps a file row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileTreeModalSheet(
    files: List<FileItem>,
    openFile: FileItem?,
    onDismiss: () -> Unit,
    onFileClick: (FileItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest    = onDismiss,
        sheetState          = sheetState,
        containerColor      = AxiomVoid,
        scrimColor          = Color.Black.copy(alpha = 0.45f),
        dragHandle          = { FileTreeHandle() }
    ) {
        val grouped = groupByDirectory(files)

        // ── Sheet header ──────────────────────────────────────────────────────
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier              = Modifier
                .fillMaxWidth()
                .height(48.dp)
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
            Spacer(Modifier.weight(1f))
            // Count badge
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AxiomSlate)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text  = files.size.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color      = AxiomTextDisabled,
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 10.sp
                    )
                )
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AxiomDusk.copy(alpha = 0.6f))
        )

        // ── File list ─────────────────────────────────────────────────────────
        val rawContentDp = run {
            val dirHeaders = grouped.keys.size * DIR_HEADER_H.value
            val fileRows   = files.size * FILE_ITEM_H.value
            val padding    = TREE_V_PADDING.value * 2
            dirHeaders + fileRows + padding
        }
        val clampedContentDp = rawContentDp.coerceAtMost(MAX_VISIBLE_H.value).dp

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
                        if (grouped.size > 1 || dir.isNotEmpty()) {
                            item(key = "dir_$dir") {
                                DirectoryHeader(path = dir)
                            }
                        }
                        itemsIndexed(dirFiles, key = { _, f -> f.id }) { _, file ->
                            FileTreeRow(
                                file     = file,
                                isActive = file.id == openFile?.id,
                                onClick  = {
                                    onFileClick(file)
                                    onDismiss()
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
        }
    }
}

// ── Grouped file map ──────────────────────────────────────────────────────────

/** Groups files by their [FileItem.path] directory. Preserves insertion order. */
private fun groupByDirectory(files: List<FileItem>): Map<String, List<FileItem>> =
    files.groupByTo(LinkedHashMap()) { it.path.trimEnd('/') }

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun FileTreeHandle() {
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
    bytes < 1_024L     -> "${bytes}B"
    bytes < 1_048_576L -> "${bytes / 1_024L}KB"
    else               -> "${"%.1f".format(bytes / 1_048_576.0)}MB"
}
