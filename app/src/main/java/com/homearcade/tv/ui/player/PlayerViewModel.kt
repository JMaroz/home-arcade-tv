package com.homearcade.tv.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homearcade.tv.data.local.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class PlayerUiState(
    val streamUrl: String? = null,
    val isLoading: Boolean = true,
    val isConfirmingExit: Boolean = false,
    val errorMessage: String? = null
)

class PlayerViewModel(
    private val romId: Int,
    private val engine: String = "webrtc",
    private val discId: Int = -1,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        initStreamUrl()
    }

    private fun initStreamUrl() {
        viewModelScope.launch {
            val serverUrl = preferences.serverUrl.firstOrNull()
            if (serverUrl.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Indirizzo del server HomeArcade non configurato."
                )
                return@launch
            }

            val profileId = preferences.activeProfileId.firstOrNull() ?: 1
            val targetId = if (discId > 0) discId else romId
            val cleanBase = serverUrl.trim().removeSuffix("/")

            val url = if (engine.equals("wasm", ignoreCase = true)) {
                "$cleanBase/api/roms/$targetId/player?profile=$profileId"
            } else {
                "$cleanBase/api/roms/$targetId/stream-player?profile=$profileId"
            }

            _uiState.value = _uiState.value.copy(
                streamUrl = url,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    fun showExitDialog() {
        _uiState.value = _uiState.value.copy(isConfirmingExit = true)
    }

    fun dismissExitDialog() {
        _uiState.value = _uiState.value.copy(isConfirmingExit = false)
    }
}
