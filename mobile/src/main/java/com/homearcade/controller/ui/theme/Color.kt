package com.homearcade.controller.ui.theme

import androidx.compose.ui.graphics.Color

val BgPrimary = Color(0xFF080A10)
val BgSecondary = Color(0xFF0E131F)
val BgCard = Color(0xFF141B2D)
val BgCardHover = Color(0xFF1C253D)

val ArcadePink = Color(0xFFEC4899)
val ArcadePinkHover = Color(0xFFDB2777)
val ArcadeViolet = Color(0xFF8B5CF6)
val AccentGold = Color(0xFFF59E0B)
val AccentGreen = Color(0xFF10B981)
val AccentRed = Color(0xFFEF4444)
val AccentBlue = Color(0xFF3B82F6)

val TextMain = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)
val TextDim = Color(0xFF64748B)

fun getSystemBadgeColor(system: String): Color {
    return when (system.lowercase().trim()) {
        "neogeo", "arcade", "mame" -> AccentGold
        "snes" -> ArcadeViolet
        "nes" -> AccentRed
        "ps1", "psx" -> AccentBlue
        "genesis", "megadrive" -> Color(0xFF06B6D4)
        else -> ArcadePink
    }
}
