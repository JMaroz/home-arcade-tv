package com.homearcade.tv.ui.settings

import android.content.Context
import android.view.InputDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homearcade.tv.data.local.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class SettingsUiState(
    val serverUrl: String = "",
    val activeProfileName: String = "",
    val preferNativeStream: Boolean = true,
    val connectedGamepads: List<String> = emptyList(),
    val appVersion: String = "1.0.0"
)

class SettingsViewModel(
    private val preferences: AppPreferences,
    context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings(context)
    }

    private fun loadSettings(context: Context) {
        viewModelScope.launch {
            val server = preferences.serverUrl.firstOrNull() ?: ""
            val profile = preferences.activeProfileName.firstOrNull() ?: "Player 1"
            val nativeStream = preferences.preferNativeStream.firstOrNull() ?: true

            // Detect connected game controllers
            val gamepads = detectConnectedGamepads()

            _uiState.value = SettingsUiState(
                serverUrl = server,
                activeProfileName = profile,
                preferNativeStream = nativeStream,
                connectedGamepads = gamepads
            )
        }
    }

    private fun detectConnectedGamepads(): List<String> {
        val deviceIds = InputDevice.getDeviceIds()
        val controllers = mutableListOf<String>()
        for (id in deviceIds) {
            val device = InputDevice.getDevice(id) ?: continue
            val sources = device.sources
            if ((sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
            ) {
                controllers.add(device.name ?: "Gamepad #$id")
            }
        }
        return controllers
    }

    fun toggleStreamingEngine() {
        val next = !_uiState.value.preferNativeStream
        _uiState.value = _uiState.value.copy(preferNativeStream = next)
        viewModelScope.launch {
            preferences.setPreferNativeStream(next)
        }
    }

    fun disconnectServer(onDisconnected: () -> Unit) {
        viewModelScope.launch {
            preferences.clear()
            onDisconnected()
        }
    }
}
