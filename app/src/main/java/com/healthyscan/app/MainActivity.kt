package com.healthyscan.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthyscan.app.ui.navigation.HealthyScanNavHost
import com.healthyscan.app.ui.theme.HealthyScanTheme
import com.healthyscan.app.ui.theme.ThemeMode
import kotlinx.coroutines.delay

/**
 * Splash screen strategy
 * =======================
 * On API 31+, the platform SplashScreen API always crops whatever icon you
 * give it into a circle/squircle mask — there is no theme flag to disable
 * that. So instead of fighting it, the OS splash here is intentionally left
 * blank (see values/themes.xml — windowSplashScreenAnimatedIcon points at a
 * 1dp transparent placeholder, background color only). The OS splash is
 * dismissed immediately, and this Activity shows its own [SplashContent]
 * composable underneath — a plain, unmasked Image of the full logo — for a
 * minimum duration, before switching to the real app content.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Let the OS splash (blank, brand-colored) dismiss immediately —
        // our own SplashContent composable takes over the visible branding.
        splashScreen.setKeepOnScreenCondition { false }

        val app = application as HealthyScanApp

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(1200) // minimum time the full, unmasked logo stays on screen
                showSplash = false
            }

            val themeMode by app.settingsRepository.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM
            )

            HealthyScanTheme(themeMode = themeMode) {
                if (showSplash) {
                    SplashContent()
                } else {
                    HealthyScanNavHost(
                        productRepository = app.productRepository,
                        settingsRepository = app.settingsRepository
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = stringResource(R.string.cd_app_logo),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(0.62f)
        )
    }
}
