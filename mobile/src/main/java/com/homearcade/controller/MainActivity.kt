package com.homearcade.controller

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.homearcade.controller.data.GamepadWebSocketClient
import com.homearcade.controller.data.LanGameRadar
import com.homearcade.controller.data.model.DiscoveredGameSession
import com.homearcade.controller.ui.gamepad.ArcadeGamepadScreen
import com.homearcade.controller.ui.lobby.DiscoveryLobbyScreen
import com.homearcade.controller.ui.theme.HomeArcadeControllerTheme

class MainActivity : ComponentActivity() {

    private lateinit var radar: LanGameRadar
    private val client = GamepadWebSocketClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on while controlling gameplay
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()

        radar = LanGameRadar(this)

        setContent {
            HomeArcadeControllerTheme {
                var activeSession by remember { mutableStateOf<DiscoveredGameSession?>(null) }
                var activeSlot by remember { mutableIntStateOf(0) }

                val session = activeSession
                if (session == null) {
                    DiscoveryLobbyScreen(
                        radar = radar,
                        onJoinSession = { selectedSession, slot ->
                            activeSlot = slot
                            activeSession = selectedSession
                        }
                    )
                } else {
                    ArcadeGamepadScreen(
                        session = session,
                        slot = activeSlot,
                        client = client,
                        onDisconnect = {
                            activeSession = null
                            radar.startScanning()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        radar.stopScanning()
        client.disconnect()
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
    }
}
