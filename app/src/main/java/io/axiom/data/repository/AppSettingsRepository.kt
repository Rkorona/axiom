package io.axiom.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Application-level singleton holding reactive user preferences.
 *
 * Lives outside any ViewModel so settings survive screen navigation.
 * In-session only — resets to defaults on cold start (DataStore persistence
 * can be wired here later without changing any call-sites).
 */
object AppSettingsRepository {

    // ── Appearance ─────────────────────────────────────────────────────────────
    private val _theme              = MutableStateFlow("dark")     // "dark"|"system"|"light"
    private val _accentKey          = MutableStateFlow("violet")   // "violet"|"coral"|"mint"
    private val _fontSize           = MutableStateFlow(14)
    private val _animatedBackground = MutableStateFlow(true)

    val theme:              StateFlow<String>  = _theme.asStateFlow()
    val accentKey:          StateFlow<String>  = _accentKey.asStateFlow()
    val fontSize:           StateFlow<Int>     = _fontSize.asStateFlow()
    val animatedBackground: StateFlow<Boolean> = _animatedBackground.asStateFlow()

    // ── Editor ─────────────────────────────────────────────────────────────────
    private val _tabSize      = MutableStateFlow(4)
    private val _wordWrap     = MutableStateFlow(false)
    private val _lineNumbers  = MutableStateFlow(true)
    private val _autoIndent   = MutableStateFlow(true)
    private val _bracketPairs = MutableStateFlow(true)

    val tabSize:      StateFlow<Int>     = _tabSize.asStateFlow()
    val wordWrap:     StateFlow<Boolean> = _wordWrap.asStateFlow()
    val lineNumbers:  StateFlow<Boolean> = _lineNumbers.asStateFlow()
    val autoIndent:   StateFlow<Boolean> = _autoIndent.asStateFlow()
    val bracketPairs: StateFlow<Boolean> = _bracketPairs.asStateFlow()

    // ── Git ────────────────────────────────────────────────────────────────────
    private val _autoFetch     = MutableStateFlow(false)
    private val _defaultBranch = MutableStateFlow("main")

    val autoFetch:     StateFlow<Boolean> = _autoFetch.asStateFlow()
    val defaultBranch: StateFlow<String>  = _defaultBranch.asStateFlow()

    // ── Setters ────────────────────────────────────────────────────────────────
    fun setTheme(theme: String)            { _theme.value              = theme }
    fun setAccent(key: String)             { _accentKey.value          = key }
    fun setFontSize(sp: Int)               { _fontSize.value           = sp }
    fun setAnimatedBackground(on: Boolean) { _animatedBackground.value = on }
    fun setTabSize(n: Int)                 { _tabSize.value            = n }
    fun setWordWrap(on: Boolean)           { _wordWrap.value           = on }
    fun setLineNumbers(on: Boolean)        { _lineNumbers.value        = on }
    fun setAutoIndent(on: Boolean)         { _autoIndent.value         = on }
    fun setBracketPairs(on: Boolean)       { _bracketPairs.value       = on }
    fun setAutoFetch(on: Boolean)          { _autoFetch.value          = on }
    fun setDefaultBranch(branch: String)   { _defaultBranch.value      = branch }
}
