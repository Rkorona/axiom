package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ExpressiveColorScheme = darkColorScheme(
    primary = ArcanePrimary,
    secondary = EmberOrange,
    tertiary = ManaTeal,
    background = VoidBackground,
    surface = SurfaceElevated,
    surfaceVariant = SurfaceSunken,
    error = BloodRose,
    onPrimary = VoidBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextMuted,
    outline = TextMuted
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ExpressiveColorScheme,
        typography = Typography,
        content = content
    )
}
