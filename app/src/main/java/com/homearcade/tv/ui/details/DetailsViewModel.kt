package com.homearcade.tv.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homearcade.tv.data.api.ApiClient
import com.homearcade.tv.data.local.AppPreferences
import com.homearcade.tv.data.model.RomDto
import com.homearcade.tv.data.model.SaveStateDto
import com.homearcade.tv.data.repository.RomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class DetailsUiState(
    val isLoading: Boolean = true,
    val serverUrl: String? = null,
    val rom: RomDto? = null,
    val discs: List<RomDto> = emptyList(),
    val selectedDiscId: Int? = null,
    val saveStates: List<SaveStateDto> = emptyList(),
    val preferNativeStream: Boolean = true,
    val errorMessage: String? = null
)

class DetailsViewModel(
    private val romId: Int,
    private val romRepository: RomRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val serverUrl = preferences.serverUrl.firstOrNull()
            val preferNative = preferences.preferNativeStream.firstOrNull() ?: true

            romRepository.getRomDetails(romId)
                .onSuccess { rom ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        serverUrl = serverUrl,
                        rom = rom,
                        selectedDiscId = rom.id,
                        preferNativeStream = preferNative
                    )

                    // If multi-disc / playlist, load discs list
                    if (rom.isPlaylist || rom.discGroup != null || rom.parentM3uId != null) {
                        loadDiscs(rom.id)
                    }

                    // Load save states
                    loadSaveStates(rom.id, serverUrl)
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        serverUrl = serverUrl,
                        errorMessage = err.message ?: "Impossibile recuperare i dettagli del gioco."
                    )
                }
        }
    }

    private fun loadDiscs(id: Int) {
        viewModelScope.launch {
            romRepository.getDiscs(id)
                .onSuccess { discList ->
                    _uiState.value = _uiState.value.copy(discs = discList)
                }
        }
    }

    private fun loadSaveStates(id: Int, baseUrl: String?) {
        if (baseUrl.isNullOrEmpty()) return
        viewModelScope.launch {
            try {
                val api = ApiClient.create(baseUrl)
                val states = api.getSaveStates(id)
                _uiState.value = _uiState.value.copy(saveStates = states)
            } catch (_: Exception) {
                // Non-blocking
            }
        }
    }

    fun selectDisc(discId: Int) {
        _uiState.value = _uiState.value.copy(selectedDiscId = discId)
    }

    fun toggleFavorite() {
        val currentRom = _uiState.value.rom ?: return
        viewModelScope.launch {
            val newStatus = !currentRom.favorite
            romRepository.toggleFavorite(currentRom.id, newStatus)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        rom = currentRom.copy(favorite = newStatus)
                    )
                }
        }
    }

    fun toggleEngine() {
        val next = !_uiState.value.preferNativeStream
        _uiState.value = _uiState.value.copy(preferNativeStream = next)
        viewModelScope.launch {
            preferences.setPreferNativeStream(next)
        }
    }
}
