package com.homearcade.tv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homearcade.tv.data.local.AppPreferences
import com.homearcade.tv.data.model.RomDto
import com.homearcade.tv.data.repository.RomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class HomeSection(val id: String, val title: String) {
    ALL("all", "Tutti"),
    FAVORITES("favorites", "Preferiti"),
    RECENT("recent", "Più Giocati"),
    SYSTEMS("systems", "Per Console")
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val serverUrl: String? = null,
    val profileName: String = "Player 1",
    val selectedSection: HomeSection = HomeSection.ALL,
    val heroRom: RomDto? = null,
    val recentlyPlayed: List<RomDto> = emptyList(),
    val favorites: List<RomDto> = emptyList(),
    val latestAdded: List<RomDto> = emptyList(),
    val systemShelves: Map<String, List<RomDto>> = emptyMap(),
    val allSystems: List<String> = emptyList(),
    val selectedSystemFilter: String? = null,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val romRepository: RomRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCatalog()
    }

    fun loadCatalog(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val serverUrl = preferences.serverUrl.firstOrNull()
            val profileName = preferences.activeProfileName.firstOrNull() ?: "Player 1"

            romRepository.fetchRoms(forceRefresh = forceRefresh)
                .onSuccess { roms ->
                    val recent = roms.filter { it.lastPlayed > 0 }.sortedByDescending { it.lastPlayed }
                    val favs = roms.filter { it.favorite }.sortedBy { it.title.lowercase() }
                    val latest = roms.sortedByDescending { it.createdAt }

                    val systemsMap = roms.groupBy { it.system.lowercase() }
                        .filter { it.value.isNotEmpty() }

                    val systemsList = systemsMap.keys.sorted()

                    // Pick hero rom: first favorite or recent or first available rom
                    val initialHero = favs.firstOrNull() ?: recent.firstOrNull() ?: roms.firstOrNull()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        serverUrl = serverUrl,
                        profileName = profileName,
                        heroRom = initialHero,
                        recentlyPlayed = recent,
                        favorites = favs,
                        latestAdded = latest,
                        systemShelves = systemsMap,
                        allSystems = systemsList,
                        errorMessage = null
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        serverUrl = serverUrl,
                        profileName = profileName,
                        errorMessage = err.message ?: "Impossibile recuperare il catalogo dei giochi."
                    )
                }
        }
    }

    fun onCardFocused(rom: RomDto) {
        _uiState.value = _uiState.value.copy(heroRom = rom)
    }

    fun onSelectSection(section: HomeSection) {
        _uiState.value = _uiState.value.copy(
            selectedSection = section,
            selectedSystemFilter = if (section == HomeSection.SYSTEMS) _uiState.value.allSystems.firstOrNull() else null
        )
    }

    fun onSelectSystemFilter(system: String) {
        _uiState.value = _uiState.value.copy(selectedSystemFilter = system)
    }

    fun toggleFavorite(rom: RomDto) {
        viewModelScope.launch {
            val newStatus = !rom.favorite
            romRepository.toggleFavorite(rom.id, newStatus)
                .onSuccess {
                    // Update in-place in UI state
                    val updatedHero = if (_uiState.value.heroRom?.id == rom.id) {
                        _uiState.value.heroRom?.copy(favorite = newStatus)
                    } else _uiState.value.heroRom

                    val updatedFavs = if (newStatus) {
                        _uiState.value.favorites + rom.copy(favorite = true)
                    } else {
                        _uiState.value.favorites.filter { it.id != rom.id }
                    }

                    _uiState.value = _uiState.value.copy(
                        heroRom = updatedHero,
                        favorites = updatedFavs
                    )
                }
        }
    }
}
