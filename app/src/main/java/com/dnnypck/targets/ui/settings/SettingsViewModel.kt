package com.dnnypck.capacitiesquicknote.ui.settings

import androidx.lifecycle.ViewModel
import com.dnnypck.capacitiesquicknote.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsScreenState(
    val apiKey: String = "",
    val spaceId: String = "",
    val message: String? = null
)

class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsScreenState())
    val state: StateFlow<SettingsScreenState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _state.update {
            it.copy(
                apiKey = preferencesManager.getApiKey(),
                spaceId = preferencesManager.getSpaceId()
            )
        }
    }

    fun updateApiKey(apiKey: String) {
        _state.update { it.copy(apiKey = apiKey) }
    }

    fun updateSpaceId(spaceId: String) {
        _state.update { it.copy(spaceId = spaceId) }
    }

    fun saveSettings() {
        val apiKey = _state.value.apiKey.trim()
        val spaceId = _state.value.spaceId.trim()

        if (apiKey.isBlank() || spaceId.isBlank()) {
            _state.update { it.copy(message = "Please fill in all fields") }
            return
        }

        preferencesManager.saveApiKey(apiKey)
        preferencesManager.saveSpaceId(spaceId)
        _state.update { it.copy(message = "Settings saved successfully") }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
