package com.healthyscan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthyscan.app.ui.navigation.HealthyScanNavHost
import com.healthyscan.app.ui.theme.HealthyScanTheme
import com.healthyscan.app.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HealthyScanApp

        setContent {
            val themeMode by app.settingsRepository.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM
            )

            HealthyScanTheme(themeMode = themeMode) {
                HealthyScanNavHost(
                    productRepository = app.productRepository,
                    settingsRepository = app.settingsRepository
                )
            }
        }
    }
}
