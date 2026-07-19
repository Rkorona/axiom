package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FantasyColorScheme = darkColorScheme(
    primary = MythicGold,
    secondary = FireAmber,
    tertiary = MagicMana,
    background = BlackObsidian,
    surface = DarkParchment,
    onPrimary = BlackObsidian,
    onBackground = SoftParchment,
    onSurface = SoftParchment,
    outline = LeatherBrown
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FantasyColorScheme,
        typography = Typography,
        content = content
    )
}
