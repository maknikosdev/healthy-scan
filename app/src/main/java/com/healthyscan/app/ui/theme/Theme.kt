package com.healthyscan.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Light mode: cream (#F8E7C9) is the background/surface, deep green (#064E3B) is
 * the primary accent used for text-on-cream, icons, and interactive elements.
 */
private val LightColors = lightColorScheme(
    primary = BrandDeepGreen,
    onPrimary = BrandCream,
    primaryContainer = GreenVariantLight,
    onPrimaryContainer = BrandCream,
    secondary = GreenVariantLight,
    onSecondary = BrandCream,
    background = BrandCream,
    onBackground = BrandDeepGreen,
    surface = BrandCream,
    onSurface = BrandDeepGreen,
    surfaceVariant = CreamVariantDark,
    onSurfaceVariant = BrandDeepGreen,
    outline = BrandDeepGreen.copy(alpha = 0.35f),
    error = ScoreVeryLow,
    onError = Color.White
)

/**
 * Dark mode: the two brand colors swap roles. Deep green (#064E3B) becomes the
 * background/surface, cream (#F8E7C9) becomes the primary accent / text color.
 */
private val DarkColors = darkColorScheme(
    primary = BrandCream,
    onPrimary = BrandDeepGreen,
    primaryContainer = GreenVariantDark,
    onPrimaryContainer = BrandCream,
    secondary = CreamVariantDark,
    onSecondary = BrandDeepGreen,
    background = BrandDeepGreen,
    onBackground = BrandCream,
    surface = BrandDeepGreen,
    onSurface = BrandCream,
    surfaceVariant = GreenVariantLight,
    onSurfaceVariant = BrandCream,
    outline = BrandCream.copy(alpha = 0.35f),
    error = ScoreVeryLow,
    onError = Color.White
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun HealthyScanTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HealthyScanTypography,
        content = content
    )
}
