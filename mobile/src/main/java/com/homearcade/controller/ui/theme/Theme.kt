package com.homearcade.controller.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ArcadePink,
    secondary = ArcadeViolet,
    background = BgPrimary,
    surface = BgCard,
    onPrimary = TextMain,
    onSecondary = TextMain,
    onBackground = TextMain,
    onSurface = TextMain
)

@Composable
fun HomeArcadeControllerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
