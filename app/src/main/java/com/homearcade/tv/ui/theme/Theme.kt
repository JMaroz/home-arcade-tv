package com.homearcade.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
private val DarkColorPalette = darkColorScheme(
    primary = ArcadePink,
    onPrimary = TextMain,
    primaryContainer = ArcadePinkHover,
    onPrimaryContainer = TextMain,
    secondary = ArcadeViolet,
    onSecondary = TextMain,
    secondaryContainer = ArcadeIndigo,
    onSecondaryContainer = TextMain,
    tertiary = AccentGold,
    onTertiary = BgPrimary,
    background = BgPrimary,
    onBackground = TextMain,
    surface = BgSecondary,
    onSurface = TextMain,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextMuted,
    error = AccentRed,
    onError = TextMain,
    border = BorderColor
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeArcadeTVTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorPalette,
        content = content
    )
}
