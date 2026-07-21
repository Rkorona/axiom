---
name: Axiom project architecture
description: Key decisions made when adding the Room data layer and project management UI to Axiom.
---

# Axiom — Data Layer + Project UI Architecture

## Core decisions

**Room setup**
- KSP (not KAPT) — matches the existing Kotlin Compose plugin pattern
- Single table: `projects` via `ProjectEntity` → `ProjectDao` → `AxiomDatabase` singleton
- All DAO queries return `Flow<List<...>>` so Room changes propagate reactively to the ViewModel

**Storage strategy (hybrid)**
- New projects: `getExternalFilesDir("projects")/<name>/` — no permission needed on minSdk 36
- Imported folders: SAF `ACTION_OPEN_DOCUMENT_TREE` + `takePersistableUriPermission()` for restart-persistent access
- `Project.isExternal` distinguishes the two: `false` = absolute path, `true` = SAF URI string

**Why:** minSdk 36 (Android 16) means dynamic colour always on; SAF is stable and requires no manifest permissions for the tree picker.

**ViewModel**
- `HomeViewModel` extends `AndroidViewModel(application)` to access context for `ProjectRepository`
- `HomeSideEffect` sealed class with `SharedFlow` for one-shot UI actions (SAF picker launch)
- `recentProjects` collected from `ProjectRepository.recentProjects(limit = 6)` Flow

**Home screen state machine**
- ① Idle: wings = project chips (`RecentProjectsWing`), bottom = `ProjectsPanel`
- ② Search active: wings collapse, bottom = `ResultsPanel`
- ③ In-project (future): wings = current project's files, bottom = file tree

**How to apply:** Any change to home screen layout must preserve the three-state machine. The dual `AnimatedVisibility` panels (`ProjectsPanel` / `ResultsPanel`) in the same `Box` use mutually exclusive `visible` conditions to avoid overlap.

**Mock data**
- `HomeViewModel.mockFiles` and `mockCommands` still in place — real file scanning is a follow-up
- `> New Project` and `> Open Folder` commands are wired end-to-end (dialog + SAF picker)
