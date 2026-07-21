package io.axiom.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.axiom.data.model.CodeLanguage
import io.axiom.data.model.Project
import io.axiom.data.util.FileSystemUtils
import io.axiom.ui.theme.AxiomSlate
import io.axiom.ui.theme.AxiomTextPrimary
import io.axiom.ui.theme.AxiomTextSecondary
import io.axiom.ui.theme.FilePathTextStyle

/**
 * A horizontal project strip card displayed in the [ProjectsPanel] idle section.
 *
 * Layout:
 * ```
 * [●] MyApp                          [Android]
 *     HomeScreen.kt  ·  2h ago
 * ```
 *
 * Spring-physics press animation matches [FileResultCard] design language.
 */
@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "project-card-scale"
    )
    val elevation by animateFloatAsState(
        targetValue   = if (isPressed) 0f else 2f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "project-card-elevation"
    )

    val langColor = Color(project.language.colorHex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation    = elevation.dp,
                shape        = RoundedCornerShape(14.dp),
                ambientColor = langColor.copy(alpha = 0.12f),
                spotColor    = langColor.copy(alpha = 0.20f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(AxiomSlate)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // ── Language colour dot ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(langColor)
            )

            Spacer(Modifier.width(12.dp))

            // ── Project info ──────────────────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier            = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text     = project.name,
                        style    = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color      = AxiomTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (project.isPinned) {
                        Icon(
                            imageVector        = Icons.Rounded.PushPin,
                            contentDescription = "Pinned",
                            tint               = langColor.copy(alpha = 0.7f),
                            modifier           = Modifier.size(12.dp)
                        )
                    }
                }

                // Subtitle: last edited file + time ago
                val subtitle = buildString {
                    project.lastEditedFile?.let { append(it).append("  ·  ") }
                    append(FileSystemUtils.timeAgo(project.lastOpened))
                }
                Text(
                    text     = subtitle,
                    style    = FilePathTextStyle.copy(
                        color    = AxiomTextSecondary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(10.dp))

            // ── Language badge ────────────────────────────────────────────────
            ProjectLanguageBadge(language = project.language, color = langColor)
        }
    }
}

@Composable
private fun ProjectLanguageBadge(
    language: CodeLanguage,
    color: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text  = language.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                color      = color,
                fontWeight = FontWeight.Bold,
                fontSize   = 10.sp
            )
        )
    }
}

// ── Wing chip ─────────────────────────────────────────────────────────────────

/**
 * Compact project chip for the Command Stage wings.
 * Mirrors [FileWingChip] with a language dot and project name.
 */
@Composable
fun ProjectWingChip(
    project: Project,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "project-wing-chip-scale"
    )

    val langColor = Color(project.language.colorHex)

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier              = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(10.dp))
            .background(AxiomSlate)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // Language colour dot
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(langColor)
        )

        Text(
            text     = project.name,
            style    = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                color      = AxiomTextPrimary,
                fontSize   = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(80.dp)
        )
    }
}
