package io.axiom.ui.editor

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * A sora-editor color scheme that matches the Axiom deep-space design language.
 *
 * Colour decisions:
 *  - Background / gutter  → AxiomInk / AxiomVoid (near-black deep-space)
 *  - Cursor / selection   → AxiomViolet (electric violet accent)
 *  - Keywords             → AxiomViolet
 *  - Strings              → AxiomMint (neon teal)
 *  - Comments             → AxiomTextDisabled (muted indigo)
 *  - Literals / numbers   → AxiomCoral
 *  - Annotations          → AxiomCoral
 *  - Operators / idents   → AxiomTextPrimary (off-white)
 */
class AxiomEditorColorScheme : EditorColorScheme() {

    override fun applyDefault() {
        super.applyDefault()

        // ── Backgrounds ────────────────────────────────────────────────────────
        setColor(WHOLE_BACKGROUND,           0xFF0C0E14.toInt()) // AxiomInk
        setColor(LINE_NUMBER_BACKGROUND,     0xFF0C0E14.toInt()) // flush with editor bg
        setColor(CURRENT_LINE,               0xFF1A1C28.toInt()) // AxiomSlate

        // ── Gutter ─────────────────────────────────────────────────────────────
        setColor(LINE_DIVIDER,               0xFF1A1C28.toInt()) // AxiomSlate — subtle sep
        setColor(LINE_NUMBER,                0xFF3A3D58.toInt()) // AxiomMist  — inactive
        setColor(LINE_NUMBER_CURRENT,        0xFF8B8FA8.toInt()) // AxiomTextSecondary

        // ── Text & selection ───────────────────────────────────────────────────
        setColor(TEXT_NORMAL,                0xFFEAEBF5.toInt()) // AxiomTextPrimary
        setColor(SELECTED_TEXT_BACKGROUND,   0x407B68EE)         // AxiomViolet @25 %

        // ── Cursor & handles ───────────────────────────────────────────────────
        setColor(SELECTION_INSERT,           0xFF7B68EE.toInt()) // AxiomViolet
        setColor(SELECTION_HANDLE,           0xFF7B68EE.toInt()) // AxiomViolet

        // ── Find / match highlight ─────────────────────────────────────────────
        setColor(MATCHED_TEXT_BACKGROUND,    0x3300DDB3)         // AxiomMint  @20 %

        // ── Syntax tokens (used when a language is attached) ───────────────────
        setColor(KEYWORD,                    0xFF7B68EE.toInt()) // AxiomViolet
        setColor(OPERATOR,                   0xFFEAEBF5.toInt()) // AxiomTextPrimary
        setColor(FUNCTION_NAME,              0xFFB0A4FF.toInt()) // AxiomVioletGlow
        setColor(IDENTIFIER_NAME,            0xFFEAEBF5.toInt()) // AxiomTextPrimary
        setColor(IDENTIFIER_VAR,             0xFFEAEBF5.toInt()) // AxiomTextPrimary
        setColor(LITERAL,                    0xFFFF6B8A.toInt()) // AxiomCoral — numbers
        setColor(COMMENT,                    0xFF4A4D68.toInt()) // AxiomTextDisabled
        setColor(ANNOTATION,                 0xFFFF6B8A.toInt()) // AxiomCoral

        // ── Diagnostics ────────────────────────────────────────────────────────
        setColor(PROBLEM_ERROR,              0xFFFF5370.toInt()) // AxiomError
        setColor(PROBLEM_WARNING,            0xFFFFC66D.toInt()) // AxiomWarning
        // Note: AUTO_COMP_PANEL_*, STRING, and PROBLEM_INFO were removed in
        // sora-editor 0.24.x. TextMate string colours are defined via scope
        // selectors when language-textmate grammars are wired up.
    }
}
