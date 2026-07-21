# Axiom — File & Code Editor

A modern Android file and code editor built with **Jetpack Compose** and **Material 3 Expressive**, featuring a non-mainstream "Command Stage" home screen design.

## Project Overview

**Package:** `io.axiom`  
**Min SDK:** 36 (Android 16) — dynamic colour always enabled  
**Target SDK:** 37  
**Stack:** Kotlin · Jetpack Compose BOM 2026.06.01 · Material 3 Expressive · ViewModel + StateFlow

## Architecture

```
app/src/main/java/io/axiom/
├── MainActivity.kt                      Entry point, edge-to-edge
├── data/
│   └── model/
│       └── FileItem.kt                  Domain models: FileItem, CodeLanguage,
│                                        AppCommand, SearchResult, GroupedResults
├── ui/
│   ├── theme/
│   │   ├── Color.kt                     Axiom brand palette (deep-space dark)
│   │   ├── Type.kt                      Full M3 type scale + Monospace extensions
│   │   └── Theme.kt                     AxiomTheme wrapping MaterialExpressiveTheme
│   ├── home/
│   │   ├── HomeUiState.kt               Immutable UI state snapshot
│   │   ├── HomeViewModel.kt             State + search logic (mock data)
│   │   └── HomeScreen.kt                Main screen orchestrator
│   └── components/
│       ├── AnimatedBackground.kt        Floating colour-orb canvas background
│       ├── CommandBar.kt                Morphing pill → search bar (centrepiece)
│       ├── RecentFilesWings.kt          Staggered file chips flanking the bar
│       ├── FileResultCard.kt            File result + FileWingChip
│       ├── CommandResultCard.kt         Command result with shortcut badge
│       ├── SectionHeader.kt             Section headers with accent marker
│       └── ResultsPanel.kt             Cascading results panel + SymbolCard
```

## Home Screen Design — "Command Stage"

```
┌──────────────────────────────────────────┐
│          [ AXIOM EDITOR ]                │  ← pill label
│    What are we building today?           │  ← greeting (fades on focus)
│   Type to search · > commands · # symbols│
│                                          │
│  [.kt][.ts]  [ 🔍  Search…  ]  [.py][.md]│  ← wings + bar
│                                          │
│         > commands  ·  # symbols         │  ← mode hints
│                                          │
├──────────────────────────────────────────┤
│  FILES                              (3)  │  ← results panel slides up
│  ┌─ MainActivity.kt ─────────── Kotlin ┐ │
│  └─ HomeScreen.kt  ─────────── Kotlin ┘ │
│  COMMANDS                               │
│  ┌─ New File ─────────────────── ⌘N   ┐ │
└──────────────────────────────────────────┘
```

**Command Bar modes:**
- Default → file fuzzy search (violet accent)
- `>` prefix → execute commands (coral accent)
- `#` prefix → jump to symbol (mint accent)

## Architecture (Updated)

```
app/src/main/java/io/axiom/
├── data/
│   ├── model/
│   │   ├── FileItem.kt           Domain models for files/search
│   │   └── Project.kt            Project domain model (id, name, rootUri, language…)
│   ├── db/
│   │   ├── ProjectEntity.kt      Room entity (projects table)
│   │   ├── ProjectDao.kt         Room DAO (Flow-based queries)
│   │   └── AxiomDatabase.kt      Singleton Room DB
│   ├── repository/
│   │   └── ProjectRepository.kt  createProject / importProject / pin / delete
│   └── util/
│       └── FileSystemUtils.kt    detectLanguage / createProjectDir / timeAgo
├── ui/
│   ├── home/
│   │   ├── HomeUiState.kt        +recentProjects, +showNewProjectDialog
│   │   ├── HomeViewModel.kt      AndroidViewModel + ProjectRepository; HomeSideEffect
│   │   └── HomeScreen.kt        SAF launcher, ProjectsPanel + ResultsPanel dual-slot
│   └── components/
│       ├── ProjectCard.kt        ProjectCard (strip card) + ProjectWingChip
│       ├── RecentProjectsWings.kt Wings with project chips (home idle state)
│       ├── ProjectsPanel.kt      Idle bottom panel with staggered project list
│       └── NewProjectDialog.kt   ModalBottomSheet for creating a project
```

## Home Screen State Machine

```
① Idle (no project open)
   Wings  = recent project chips
   Bottom = ProjectsPanel (project cards, staggered entrance)

② Search active (bar focused)
   Wings  = collapse off-screen
   Bottom = ResultsPanel (files / commands / symbols)

③ In-project (future)
   Wings  = current project's recent files
   Bottom = file tree / editor tabs
```

## Project Storage (Hybrid)
- **New project** → `getExternalFilesDir("projects")/<name>/` — no permission needed
- **Import folder** → SAF `ACTION_OPEN_DOCUMENT_TREE` + `takePersistableUriPermission()` — survives restarts

## Running the Project

This is an Android project — open in **Android Studio** or build via Gradle:

```bash
./gradlew assembleDebug
```

Deploy to a connected device or emulator (API 36+):

```bash
./gradlew installDebug
```

## User Preferences

- **Modular code**: Each component in its own file, single responsibility
- **Complete, non-minified code**: No shortcuts — animations and interactions fully implemented
- **Modern, non-mainstream design**: Diverge from standard Material Design patterns
- **Animations拉满**: Spring physics everywhere, staggered entrances, morphing shapes
