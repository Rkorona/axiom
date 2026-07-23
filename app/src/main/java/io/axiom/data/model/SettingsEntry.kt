package io.axiom.data.model

// ── Settings data model ────────────────────────────────────────────────────────

enum class SettingsCategory(
    val label: String,
    val commandMode: CommandMode   // drives AnimatedBackground colour
) {
    APPEARANCE("APPEARANCE", CommandMode.FILE),
    EDITOR("EDITOR",         CommandMode.SYMBOL),
    GIT("GIT",               CommandMode.COMMAND),
    ABOUT("ABOUT",           CommandMode.FILE)
}

sealed class SettingValue {
    data class Toggle(val enabled: Boolean)                              : SettingValue()
    data class Select(val options: List<String>, val selected: String)   : SettingValue()
    data class Stepper(val value: Int, val min: Int, val max: Int, val step: Int = 1) : SettingValue()
    data class AccentPicker(val selected: String)                        : SettingValue()   // "violet" | "coral" | "mint"
    data class Info(val text: String)                                    : SettingValue()
}

data class SettingsEntry(
    val id: String,
    val key: String,
    val comment: String,
    val value: SettingValue,
    val category: SettingsCategory
)

fun defaultSettings(): List<SettingsEntry> = listOf(

    // ── APPEARANCE ─────────────────────────────────────────────────────────────
    SettingsEntry("theme",              "theme",             "// colour scheme",
        SettingValue.Select(listOf("dark", "system", "light"), "dark"), SettingsCategory.APPEARANCE),
    SettingsEntry("accentColor",        "accentColor",       "// primary interface accent",
        SettingValue.AccentPicker("violet"),                             SettingsCategory.APPEARANCE),
    SettingsEntry("fontSize",           "fontSize",          "// editor base font size (sp)",
        SettingValue.Stepper(14, 10, 20),                               SettingsCategory.APPEARANCE),
    SettingsEntry("animatedBackground", "animatedBackground","// drifting orb background",
        SettingValue.Toggle(true),                                       SettingsCategory.APPEARANCE),

    // ── EDITOR ─────────────────────────────────────────────────────────────────
    SettingsEntry("tabSize",     "tabSize",    "// spaces per tab stop",
        SettingValue.Stepper(4, 2, 8, 2),   SettingsCategory.EDITOR),
    SettingsEntry("wordWrap",    "wordWrap",   "// wrap long lines",
        SettingValue.Toggle(false),          SettingsCategory.EDITOR),
    SettingsEntry("lineNumbers", "lineNumbers","// gutter line numbers",
        SettingValue.Toggle(true),           SettingsCategory.EDITOR),
    SettingsEntry("autoIndent",  "autoIndent", "// auto-indent on newline",
        SettingValue.Toggle(true),           SettingsCategory.EDITOR),
    SettingsEntry("bracketPairs","bracketPairs","// highlight matching brackets",
        SettingValue.Toggle(true),           SettingsCategory.EDITOR),

    // ── GIT ────────────────────────────────────────────────────────────────────
    SettingsEntry("autoFetch",     "autoFetch",    "// fetch on project open",
        SettingValue.Toggle(false),   SettingsCategory.GIT),
    SettingsEntry("defaultBranch", "defaultBranch","// branch for new projects",
        SettingValue.Select(listOf("main", "master", "develop"), "main"), SettingsCategory.GIT),

    // ── ABOUT ──────────────────────────────────────────────────────────────────
    SettingsEntry("version",   "version",   "// application version",
        SettingValue.Info("1.0.0"),     SettingsCategory.ABOUT),
    SettingsEntry("buildDate", "buildDate", "// release date",
        SettingValue.Info("2026-07-23"),SettingsCategory.ABOUT),
    SettingsEntry("license",   "license",   "// software licence",
        SettingValue.Info("MIT"),       SettingsCategory.ABOUT),
)
