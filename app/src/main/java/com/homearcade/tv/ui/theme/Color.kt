package com.homearcade.tv.ui.theme

import androidx.compose.ui.graphics.Color

// Backgrounds
val BgPrimary = Color(0xFF080A10)
val BgSecondary = Color(0xFF0E131F)
val BgCard = Color(0xFF141B2D)
val BgCardHover = Color(0xFF1C253D)
val BgModal = Color(0xFF0D121F)

// Primary Retro Arcade Brand
val ArcadePink = Color(0xFFEC4899)
val ArcadePinkHover = Color(0xFFDB2777)
val ArcadeGlow = Color(0x66EC4899)
val FocusBorderColor = Color(0xFFF472B6)

// Accents
val ArcadeViolet = Color(0xFF8B5CF6)
val ArcadeIndigo = Color(0xFF6366F1)
val AccentGold = Color(0xFFF59E0B)
val AccentCyan = Color(0xFF06B6D4)
val AccentGreen = Color(0xFF10B981)
val AccentRed = Color(0xFFEF4444)

// Typography & Borders
val TextMain = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)
val TextDim = Color(0xFF64748B)
val BorderColor = Color(0x1FFFFFFF)

// Legacy aliases for backward compatibility if referenced
val Pink = ArcadePink
val PinkDark = ArcadePinkHover
val DarkBg = BgPrimary
val DarkSurface = BgSecondary
val White = TextMain
val Green = AccentGreen
val Red = AccentRed
val Amber = AccentGold
val SurfaceVariant = BgCard

/**
 * Console-specific retro color mapping for badges & cards.
 */
fun getSystemColor(system: String): Color {
    return when (system.lowercase().trim()) {
        "snes" -> Color(0xFF8B5CF6)
        "nes" -> Color(0xFFEF4444)
        "ps1", "psx" -> Color(0xFF3B82F6)
        "ps2" -> Color(0xFF2563EB)
        "n64" -> Color(0xFF10B981)
        "gba" -> Color(0xFFA855F7)
        "gb", "gbc" -> Color(0xFF14B8A6)
        "genesis", "megadrive", "md" -> Color(0xFFF97316)
        "arcade", "mame", "fbneo" -> Color(0xFFEC4899)
        "dreamcast", "dc" -> Color(0xFFF59E0B)
        "nds" -> Color(0xFF06B6D4)
        "psp" -> Color(0xFF6366F1)
        else -> ArcadePink
    }
}
