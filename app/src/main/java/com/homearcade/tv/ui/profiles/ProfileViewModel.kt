package com.homearcade.tv.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homearcade.tv.data.model.ProfileDto
import com.homearcade.tv.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profiles = MutableStateFlow<List<ProfileDto>>(emptyList())
    val profiles: StateFlow<List<ProfileDto>> = _profiles.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadProfiles()
    }

    fun loadProfiles() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            profileRepository.getProfiles()
                .onSuccess { list ->
                    _profiles.value = list
                    _isLoading.value = false
                }
                .onFailure { err ->
                    _errorMessage.value = err.message ?: "Impossibile caricare i profili."
                    _isLoading.value = false
                }
        }
    }

    fun onProfileSelected(profile: ProfileDto, onConfirmed: () -> Unit) {
        viewModelScope.launch {
            profileRepository.selectProfile(profile)
            onConfirmed()
        }
    }
}
