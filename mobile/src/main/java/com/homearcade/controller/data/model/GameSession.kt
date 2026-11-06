package com.homearcade.controller.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DiscoveredGameSession(
    val hostIp: String,
    val port: Int = 9877,
    val gameTitle: String = "HomeArcade Game",
    val platform: String = "arcade",
    val romId: Int = 0,
    val serverName: String = "HomeArcade TV",
    val defaultSlot: Int = 0
)

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class GamepadLayoutMode {
    ARCADE_4_BTN, // Neo Geo / Arcade Arc (A: Shoot, B: Jump, C: Grenade, D: Special)
    CONSOLE_DIAMOND // Standard Diamond (A, B, X, Y + L, R)
}
