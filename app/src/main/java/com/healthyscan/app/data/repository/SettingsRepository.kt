package com.healthyscan.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.healthyscan.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "healthyscan_settings")

/**
 * Stores everything that should survive app restarts but doesn't need SQL querying:
 * theme choice, onboarding completion + selected dietary preferences.
 * Language is intentionally NOT stored here — AppCompatDelegate persists it itself.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val DIETARY_PREFERENCES = stringSetPreferencesKey("dietary_preferences")
        val AVOIDED_ALLERGENS = stringSetPreferencesKey("avoided_allergens")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.THEME_MODE]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.ONBOARDING_DONE] ?: false
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    val dietaryPreferences: Flow<Set<String>> = context.dataStore.data.map {
        it[Keys.DIETARY_PREFERENCES] ?: emptySet()
    }

    suspend fun setDietaryPreferences(preferences: Set<String>) {
        context.dataStore.edit { it[Keys.DIETARY_PREFERENCES] = preferences }
    }

    val avoidedAllergens: Flow<Set<String>> = context.dataStore.data.map {
        it[Keys.AVOIDED_ALLERGENS] ?: emptySet()
    }

    suspend fun setAvoidedAllergens(allergens: Set<String>) {
        context.dataStore.edit { it[Keys.AVOIDED_ALLERGENS] = allergens }
    }
}
