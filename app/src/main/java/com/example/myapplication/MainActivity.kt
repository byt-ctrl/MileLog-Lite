package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.navigation.MileLiteNavHost
import com.example.myapplication.ui.theme.MileLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = (application as MileLogApplication).settingsRepository

        setContent {
            // Collected at the root, so changing the appearance in Settings
            // recomposes every screen instead of waiting for a restart.
            val themeMode by settings.themeMode.collectAsState()
            val darkTheme = themeMode.isDark(isSystemInDarkTheme())

            // Edge-to-edge with explicit system-bar styling so the status and
            // navigation bars blend with the surface instead of falling back to
            // platform defaults. Both bars stay transparent; the icon
            // foreground follows the appearance the logbook resolved to, which
            // is the user's choice rather than the device's.
            LaunchedEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT
                    ) { darkTheme }
                )
            }

            MileLogTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MileLiteNavHost()
                }
            }
        }
    }
}
