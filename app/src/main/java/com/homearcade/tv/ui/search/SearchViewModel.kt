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
    val serverUrl: String? = null
)

class SearchViewModel(
    private val romRepository: RomRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val url = preferences.serverUrl.firstOrNull()
            _uiState.value = _uiState.value.copy(serverUrl = url)
        }
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        searchJob?.cancel()

        if (newQuery.trim().isEmpty()) {
            _uiState.value = _uiState.value.copy(isSearching = false, results = emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            _uiState.value = _uiState.value.copy(isSearching = true)
            val matched = romRepository.searchRoms(newQuery)
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                results = matched
            )
        }
    }
}
