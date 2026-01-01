package com.dnnypck.capacitiesquicknote.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnnypck.capacitiesquicknote.data.network.postToTarget
import com.dnnypck.capacitiesquicknote.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainScreenState(
    val content: String = "",
    val isSending: Boolean = false,
    val message: String? = null,
    val hasCredentials: Boolean = false
)

class MainViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    init {
        checkCredentials()
    }

    fun checkCredentials() {
        _state.update { it.copy(hasCredentials = preferencesManager.hasRequiredSettings()) }
    }

    fun updateContent(content: String) {
        _state.update { it.copy(content = content) }
    }

    fun sendContent() {
        val content = _state.value.content
        if (content.isBlank()) return

        if (!preferencesManager.hasRequiredSettings()) {
            _state.update { it.copy(message = "Please configure API settings first") }
            return
        }

        val apiKey = preferencesManager.getApiKey()
        val spaceId = preferencesManager.getSpaceId()

        viewModelScope.launch {
            _state.update { it.copy(isSending = true, message = null) }

            try {
                val headers = mapOf(
                    "Authorization" to "Bearer $apiKey",
                    "Content-Type" to "application/json"
                )

                val body = """
                    {
                        "spaceId": "$spaceId",
                        "mdText": "${content.replace("\"", "\\\"").replace("\n", "\\n")}",
                        "origin": "commandPalette"
                    }
                """.trimIndent()

                val result = postToTarget(
                    url = "https://api.capacities.io/save-to-daily-note",
                    headers = headers,
                    body = body
                )

                result.fold(
                    onSuccess = {
                        _state.update {
                            it.copy(
                                isSending = false,
                                message = "Successfully saved to Capacities",
                                content = ""
                            )
                        }
                    },
                    onFailure = { error ->
                        val detailedError = buildString {
                            appendLine("Failed to send to Capacities")
                            appendLine()
                            appendLine("Error: ${error.message}")
                            appendLine()
                            appendLine("Request details:")
                            appendLine("URL: https://api.capacities.io/save-to-daily-note")
                            appendLine("Space ID: $spaceId")
                            appendLine("API Key: ${apiKey.take(8)}...")
                            appendLine()
                            appendLine("Request body:")
                            appendLine(body)
                        }
                        _state.update {
                            it.copy(
                                isSending = false,
                                message = detailedError
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSending = false,
                        message = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
