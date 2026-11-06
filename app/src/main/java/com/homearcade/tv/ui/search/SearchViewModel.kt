package com.homearcade.tv.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homearcade.tv.data.local.AppPreferences
import com.homearcade.tv.data.model.RomDto
import com.homearcade.tv.data.repository.RomRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<RomDto> = emptyList(),
    val initialCatalog: List<RomDto> = emptyList(),
    val serverUrl: String? = null
)

class SearchViewModel(
    private val romRepository: RomRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val quickTags = listOf("Tutti", "Arcade", "SNES", "PS1", "GBA", "NES", "Genesis", "Mario", "Sonic", "Metal Slug")

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val url = preferences.serverUrl.firstOrNull()
            val allRoms = romRepository.fetchRoms().getOrNull() ?: emptyList()
            _uiState.value = _uiState.value.copy(
                serverUrl = url,
                results = allRoms,
                initialCatalog = allRoms
            )
        }
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        searchJob?.cancel()

        if (newQuery.trim().isEmpty() || newQuery.equals("Tutti", ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                query = if (newQuery.equals("Tutti", ignoreCase = true)) "" else newQuery,
                results = _uiState.value.initialCatalog
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(250) // 250ms debounce
            _uiState.value = _uiState.value.copy(isSearching = true)
            val matched = romRepository.searchRoms(newQuery)
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                results = matched
            )
        }
    }
}
