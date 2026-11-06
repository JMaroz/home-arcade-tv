package com.homearcade.tv.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homearcade.tv.data.local.AppPreferences
import com.homearcade.tv.data.repository.RomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class PlayerUiState(
    val streamUrl: String? = null,
    val isLoading: Boolean = true,
    val isConfirmingExit: Boolean = false,
    val errorMessage: String? = null,
    val gameTitle: String = "HomeArcade Game",
    val platform: String = "arcade",
    val connectedSlots: Set<Int> = emptySet(),
    val latestConnectedSlotNotification: Int? = null
)

class PlayerViewModel(
    val romId: Int,
    private val engine: String = "webrtc",
    private val discId: Int = -1,
    private val preferences: AppPreferences,
    private val romRepository: RomRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        initGameMetadata()
        initStreamUrl()
    }

    private fun initGameMetadata() {
        viewModelScope.launch {
            if (romRepository != null && romId > 0) {
                try {
                    val romResult = romRepository.getRomDetails(romId)
                    romResult.getOrNull()?.let { rom ->
                        _uiState.value = _uiState.value.copy(
                            gameTitle = rom.title,
                            platform = rom.system
                        )
                    }
                } catch (_: Exception) {
                    // Fallback to defaults
                }
            }
        }
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

    fun updateConnectedSlot(slot: Int, isConnected: Boolean) {
        val current = _uiState.value.connectedSlots.toMutableSet()
        if (isConnected) {
            current.add(slot)
            _uiState.value = _uiState.value.copy(
                connectedSlots = current,
                latestConnectedSlotNotification = slot
            )
        } else {
            current.remove(slot)
            _uiState.value = _uiState.value.copy(
                connectedSlots = current
            )
        }
    }

    fun clearSlotNotification() {
        _uiState.value = _uiState.value.copy(latestConnectedSlotNotification = null)
    }
}
