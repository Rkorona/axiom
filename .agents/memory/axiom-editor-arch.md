---
name: Axiom editor screen architecture
description: EditorScreen layout, FileTreeModalSheet design, CommandBar folder button — how the editor is structured and why it was redesigned
---

# Axiom Editor Screen Architecture

## Current design (Phase A complete + file-tree redesign)

### EditorScreen layout
```
Box (full screen)
  Layer 1: AnimatedBackground
  Layer 2: scrim (animates in when CommandBar focused)
  Layer 3: Column
    EditorTopBar  (back · project name · open file · save)
    Box (weight 1f)
      EditorSurface / LoadingOverlay / EditorEmptyState
      ResultsPanel (overlays while CommandBar focused + query non-empty)
    CommandBar  ← folder icon on left, search in centre
    Spacer(navigationBarsPadding)
  FileTreeModalSheet  (shown when showFileTree == true)
```

### FileTreeModalSheet
- Replaces the old custom draggable peek-sheet (`FileTreeSheet`)
- Uses M3 `ModalBottomSheet(skipPartiallyExpanded = true)` — handles all insets, drag-to-dismiss, scrim, back press
- **Why replaced**: the old sheet had a WindowInsets timing race — `WindowInsets.navigationBars.getBottom()` returned 0 on first compose, causing `collapsedOffset` to be wrong. One frame later insets settled and `LaunchedEffect` snapped the sheet, producing a visible flicker on every editor entry.
- **How to apply**: never re-introduce a custom `BoxWithConstraints` + `WindowInsets.getBottom()` pattern for bottom sheet positioning. Always prefer `ModalBottomSheet`.

### CommandBar folder button
- `CommandBar` accepts `onFileTreeClick: (() -> Unit)? = null`
- When non-null: renders a `📁` `IconButton` (32dp) + 1dp vertical divider before the `CommandModeIndicator`
- Home screen passes `null` (default) — no button shown there
- **Why optional**: CommandBar is shared between HomeScreen (no project) and EditorScreen (project open). Making it optional keeps the home screen unchanged.

### Navigation
- `AxiomNavGraph.kt` connects HomeScreen → EditorScreen with a shared-element transition on the CommandBar pill (`key = "command-bar"`)
- EditorScreen uses `BackHandler(enabled = focused)` to clear focus before popping back stack

### Empty state
- When no file is open: shows "Tap the folder icon to open a file" guidance (updated from old "Drag up" which referenced the removed peek-sheet)
