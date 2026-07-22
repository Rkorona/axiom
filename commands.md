# Axiom — Command Bar 命令清单

> 用户在搜索栏输入 `>` 前缀后进入 Command 模式，以下命令通过模糊匹配显示。

---

## 主页搜索栏（HomeViewModel · `mockCommands`）

共 **18 条**，仅 `new_project`、`open_folder` 已实现逻辑，其余为 stub。

| # | ID | 显示名称 | 描述 | 分类 | 快捷键 | 状态 |
|---|---|---|---|---|---|---|
| 1 | `new_project` | New Project | Create a new project in Axiom | File | — | ✅ 已实现 |
| 2 | `open_folder` | Open Folder | Import an existing folder as a project | File | — | ✅ 已实现 |
| 3 | `new_file` | New File | Create a new file in the current directory | File | ⌘N | ⚠️ Stub |
| 4 | `new_folder` | New Folder | Create a new folder | File | ⌘⇧N | ⚠️ Stub |
| 5 | `save_all` | Save All | Save all unsaved changes | File | ⌘⇧S | ⚠️ Stub |
| 6 | `close_file` | Close File | Close the active editor tab | File | ⌘W | ⚠️ Stub |
| 7 | `find_replace` | Find & Replace | Search and replace across all project files | Edit | ⌘⇧F | ⚠️ Stub |
| 8 | `format_doc` | Format Document | Run the code formatter on the current file | Edit | ⌘⇧I | ⚠️ Stub |
| 9 | `rename_symbol` | Rename Symbol | Refactor: rename symbol under cursor | Edit | F2 | ⚠️ Stub |
| 10 | `open_terminal` | Open Terminal | Open integrated terminal panel | Terminal | ⌘\` | ⚠️ Stub |
| 11 | `split_editor` | Split Editor | Split editor horizontally or vertically | View | ⌘\\ | ⚠️ Stub |
| 12 | `toggle_minimap` | Toggle Minimap | Show or hide the code minimap | View | — | ⚠️ Stub |
| 13 | `toggle_theme` | Toggle Theme | Switch between dark and light colour scheme | View | — | ⚠️ Stub |
| 14 | `git_status` | Git Status | Show working tree status | Git | — | ⚠️ Stub |
| 15 | `git_commit` | Git Commit | Stage all changes and open commit dialog | Git | — | ⚠️ Stub |
| 16 | `git_push` | Git Push | Push commits to remote | Git | — | ⚠️ Stub |
| 17 | `settings` | Settings | Open application settings | General | ⌘, | ⚠️ Stub |
| 18 | `command_palette` | Command Palette | Open this command palette | General | ⌘⇧P | ⚠️ Stub |

---

## 编辑器搜索栏（EditorViewModel · `EDITOR_COMMANDS`）

共 **8 条**，仅 `save` 已实现逻辑，其余为 stub。

| # | ID | 显示名称 | 描述 | 分类 | 快捷键 | 状态 |
|---|---|---|---|---|---|---|
| 1 | `save` | Save File | Save the currently open file | File | — | ✅ 已实现（仅限内部项目） |
| 2 | `new_file` | New File | Create a new file in this project | File | — | ⚠️ Stub |
| 3 | `close_file` | Close File | Close the active file | File | — | ⚠️ Stub |
| 4 | `find_replace` | Find & Replace | Search and replace in current file | Edit | — | ⚠️ Stub |
| 5 | `format_doc` | Format Document | Run code formatter on current file | Edit | — | ⚠️ Stub |
| 6 | `git_status` | Git Status | Show working tree status | Git | — | ⚠️ Stub |
| 7 | `git_commit` | Git Commit | Stage changes and commit | Git | — | ⚠️ Stub |
| 8 | `settings` | Settings | Open application settings | General | — | ⚠️ Stub |

---

## 两端重复命令对比

以下命令在两个搜索栏中都有定义，但描述文字略有差异，未共享定义：

| ID | 主页描述 | 编辑器描述 |
|---|---|---|
| `new_file` | Create a new file in the current directory | Create a new file in this project |
| `close_file` | Close the active editor tab | Close the active file |
| `find_replace` | Search and replace **across all project files** | Search and replace **in current file** |
| `format_doc` | Run the code formatter on the current file | Run code formatter on current file |
| `git_status` | Show working tree status | Show working tree status |
| `git_commit` | Stage **all changes** and open commit dialog | Stage changes and commit |
| `settings` | Open application settings | Open application settings |

---

## 主页命令合理性分析

### ✅ 主页合理的命令（与项目/App 全局相关）

| ID | 理由 |
|---|---|
| `new_project` | 核心主页操作 |
| `open_folder` | 核心主页操作 |
| `toggle_theme` | App 全局设置，任何页面均可触发 |
| `settings` | App 全局设置 |
| `command_palette` | 元命令，全局可用 |

### ❌ 主页不合理的命令（需要编辑器上下文）

| ID | 问题 |
|---|---|
| `find_replace` | 需要已打开的文件，主页无编辑器实例 |
| `format_doc` | 需要已打开的文件 |
| `rename_symbol` | 需要光标位置和语法树，主页完全无法执行 |
| `close_file` | 主页没有打开任何文件，无意义 |
| `save_all` | 主页无文件 buffer，无意义 |
| `split_editor` | 主页无编辑器，无意义 |
| `toggle_minimap` | 主页无编辑器，无意义 |

### 🟡 主页存疑的命令（有一定道理但依赖项目上下文）

| ID | 分析 |
|---|---|
| `new_file` | 主页没有"当前目录"的概念，需先选中一个项目 |
| `new_folder` | 同上 |
| `open_terminal` | 若终端是全局面板则合理，若绑定项目则需项目上下文 |
| `git_status` | 若有全局 Git 视图则合理，否则需先打开项目 |
| `git_commit` | 同上 |
| `git_push` | 同上 |
