package io.axiom.ui.settings

import androidx.lifecycle.ViewModel
import io.axiom.data.model.SettingsCategory
import io.axiom.data.model.SettingValue
import io.axiom.data.repository.AppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onCategorySelect(category: SettingsCategory) {
        _uiState.update { it.copy(activeCategory = category) }
    }

    fun onToggle(entryId: String) {
        _uiState.update { state ->
            state.copy(entries = state.entries.map { entry ->
                if (entry.id == entryId && entry.value is SettingValue.Toggle)
                    entry.copy(value = entry.value.copy(enabled = !entry.value.enabled))
                else entry
            })
        }
        _uiState.value.entries.find { it.id == entryId }
            ?.let { syncToRepository(it.id, it.value) }
    }

    fun onSelectChange(entryId: String, selected: String) {
        _uiState.update { state ->
            state.copy(entries = state.entries.map { entry ->
                if (entry.id == entryId && entry.value is SettingValue.Select)
                    entry.copy(value = entry.value.copy(selected = selected))
                else entry
            })
        }
        _uiState.value.entries.find { it.id == entryId }
            ?.let { syncToRepository(it.id, it.value) }
    }

    fun onStepperChange(entryId: String, delta: Int) {
        _uiState.update { state ->
            state.copy(entries = state.entries.map { entry ->
                if (entry.id == entryId && entry.value is SettingValue.Stepper) {
                    val v = entry.value
                    entry.copy(value = v.copy(value = (v.value + delta).coerceIn(v.min, v.max)))
                } else entry
            })
        }
        _uiState.value.entries.find { it.id == entryId }
            ?.let { syncToRepository(it.id, it.value) }
    }

    fun onAccentChange(entryId: String, accent: String) {
        _uiState.update { state ->
            state.copy(entries = state.entries.map { entry ->
                if (entry.id == entryId && entry.value is SettingValue.AccentPicker)
                    entry.copy(value = SettingValue.AccentPicker(accent))
                else entry
            })
        }
        _uiState.value.entries.find { it.id == entryId }
            ?.let { syncToRepository(it.id, it.value) }
    }

    // ── Repository sync ────────────────────────────────────────────────────────

    /** Pushes a changed setting value to [AppSettingsRepository] so the rest of
     *  the app reacts in real-time. */
    private fun syncToRepository(id: String, value: SettingValue) {
        when (id) {
            "accentColor"        -> (value as? SettingValue.AccentPicker)
                ?.let { AppSettingsRepository.setAccent(it.selected) }
            "fontSize"           -> (value as? SettingValue.Stepper)
                ?.let { AppSettingsRepository.setFontSize(it.value) }
            "animatedBackground" -> (value as? SettingValue.Toggle)
                ?.let { AppSettingsRepository.setAnimatedBackground(it.enabled) }
            "tabSize"            -> (value as? SettingValue.Stepper)
                ?.let { AppSettingsRepository.setTabSize(it.value) }
            "wordWrap"           -> (value as? SettingValue.Toggle)
                ?.let { AppSettingsRepository.setWordWrap(it.enabled) }
            "lineNumbers"        -> (value as? SettingValue.Toggle)
                ?.let { AppSettingsRepository.setLineNumbers(it.enabled) }
            "autoIndent"         -> (value as? SettingValue.Toggle)
                ?.let { AppSettingsRepository.setAutoIndent(it.enabled) }
            "bracketPairs"       -> (value as? SettingValue.Toggle)
                ?.let { AppSettingsRepository.setBracketPairs(it.enabled) }
        }
    }
}
