package io.axiom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.axiom.data.model.CodeLanguage
import io.axiom.ui.theme.AxiomDusk
import io.axiom.ui.theme.AxiomFileModeColor
import io.axiom.ui.theme.AxiomInk
import io.axiom.ui.theme.AxiomSlate
import io.axiom.ui.theme.AxiomTextDisabled
import io.axiom.ui.theme.AxiomTextPrimary
import io.axiom.ui.theme.AxiomTextSecondary
import io.axiom.ui.theme.AxiomMist
import io.axiom.ui.theme.AxiomVoid

/**
 * Bottom sheet dialog for creating a new project.
 *
 * Lets the user:
 * 1. Enter a project name.
 * 2. Optionally pick a primary language (defaults to Unknown / auto-detect).
 *
 * On confirm, [onCreate] is called with the chosen name and language.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, language: CodeLanguage) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartialExpansion = true)

    var projectName by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(CodeLanguage.UNKNOWN) }
    val focusRequester = remember { FocusRequester() }

    // Auto-focus the text field when the sheet opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        sheetState        = sheetState,
        containerColor    = AxiomVoid,
        dragHandle        = {
            // Custom drag handle
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AxiomMist)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Text(
                text  = "New Project",
                style = MaterialTheme.typography.titleMedium.copy(
                    color      = AxiomTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text  = "Created in your private app storage — no permissions needed.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color    = AxiomTextSecondary,
                    fontSize = 12.sp
                )
            )

            Spacer(Modifier.height(20.dp))

            // Project name field
            OutlinedTextField(
                value         = projectName,
                onValueChange = { projectName = it },
                label         = { Text("Project name") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction      = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (projectName.isNotBlank()) {
                            onCreate(projectName.trim(), selectedLanguage)
                        }
                    }
                ),
                colors  = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AxiomFileModeColor,
                    focusedLabelColor    = AxiomFileModeColor,
                    unfocusedBorderColor = AxiomDusk,
                    unfocusedLabelColor  = AxiomTextDisabled,
                    cursorColor          = AxiomFileModeColor,
                    focusedTextColor     = AxiomTextPrimary,
                    unfocusedTextColor   = AxiomTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            Spacer(Modifier.height(20.dp))

            // Language picker
            Text(
                text  = "Language  (optional — auto-detected if blank)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = AxiomTextSecondary,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(Modifier.height(10.dp))

            val displayLanguages = listOf(
                CodeLanguage.UNKNOWN,
                CodeLanguage.KOTLIN,
                CodeLanguage.JAVA,
                CodeLanguage.JAVASCRIPT,
                CodeLanguage.TYPESCRIPT,
                CodeLanguage.PYTHON,
                CodeLanguage.RUST,
                CodeLanguage.GO,
                CodeLanguage.SWIFT,
                CodeLanguage.DART
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                displayLanguages.forEach { lang ->
                    LanguageChip(
                        language   = lang,
                        isSelected = lang == selectedLanguage,
                        onClick    = { selectedLanguage = lang }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text  = "Cancel",
                        color = AxiomTextSecondary
                    )
                }

                Button(
                    onClick  = {
                        if (projectName.isNotBlank()) {
                            onCreate(projectName.trim(), selectedLanguage)
                        }
                    },
                    enabled  = projectName.isNotBlank(),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = AxiomFileModeColor,
                        contentColor   = AxiomInk
                    ),
                    modifier = Modifier.weight(2f)
                ) {
                    Text(
                        text       = "Create",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Language chip ─────────────────────────────────────────────────────────────

@Composable
private fun LanguageChip(
    language: CodeLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val langColor = if (language == CodeLanguage.UNKNOWN) AxiomTextSecondary
                    else Color(language.colorHex)

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier              = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) langColor.copy(alpha = 0.20f)
                else AxiomSlate
            )
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        if (language != CodeLanguage.UNKNOWN) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(langColor)
            )
        }
        Text(
            text  = if (language == CodeLanguage.UNKNOWN) "Auto" else language.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                color      = if (isSelected) langColor else AxiomTextSecondary,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize   = 11.sp
            )
        )
    }
}
