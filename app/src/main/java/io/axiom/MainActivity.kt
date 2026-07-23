package io.axiom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.axiom.data.repository.AppSettingsRepository
import io.axiom.ui.navigation.AxiomNavGraph
import io.axiom.ui.theme.AxiomTheme

/**
 * Single-activity entry point for Axiom.
 *
 * Enables edge-to-edge rendering so the [AnimatedBackground] canvas
 * bleeds behind the status and navigation bars, then hands off
 * everything to the Compose [HomeScreen].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePref by AppSettingsRepository.theme.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themePref) {
                "dark"   -> true
                "light"  -> false
                else     -> systemDark   // "system"
            }
            AxiomTheme(darkTheme = darkTheme) {
                AxiomNavGraph()
            }
        }
    }
}
