package com.healthyscan.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthyscan.app.ui.navigation.HealthyScanNavHost
import com.healthyscan.app.ui.theme.HealthyScanTheme
import com.healthyscan.app.ui.theme.ThemeMode
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startTimeMillis = System.currentTimeMillis()
        var isAppReady = false
        splashScreen.setKeepOnScreenCondition { !isAppReady }

        val app = application as HealthyScanApp

        setContent {
            LaunchedEffect(Unit) {
                val elapsed = System.currentTimeMillis() - startTimeMillis
                val remaining = 1000L - elapsed
                if (remaining > 0) delay(remaining)
                isAppReady = true
            }

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