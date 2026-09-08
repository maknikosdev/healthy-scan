package com.healthyscan.app.data.backup

import android.content.Context
import com.google.gson.Gson
import com.healthyscan.app.data.local.AppDatabase
import com.healthyscan.app.data.local.BasketItemEntity
import com.healthyscan.app.data.local.FavoriteEntity
import com.healthyscan.app.data.local.ScanHistoryEntity
import com.healthyscan.app.data.repository.SettingsRepository
import com.healthyscan.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.first

/**
 * Everything the app knows about the person, in one flat, human-readable
 * JSON structure — used for the "export to a file" / "import on another
 * device" feature in Profile. Deliberately does NOT include the Open Food
 * Facts password: only the username travels with the backup, so importing
 * on a new device still asks for the password once (safer than having a
 * plaintext password sitting in a JSON file that might get emailed, synced
 * to a cloud drive, etc.).
 */
data class BackupData(
    val version: Int = 1,
    val exportedAtMillis: Long = System.currentTimeMillis(),
    val history: List<ScanHistoryEntity> = emptyList(),
    val favorites: List<FavoriteEntity> = emptyList(),
    val basket: List<BasketItemEntity> = emptyList(),
    val dietaryPreferences: List<String> = emptyList(),
    val avoidedAllergens: List<String> = emptyList(),
    val themeMode: String = ThemeMode.SYSTEM.name,
    val onboardingDone: Boolean = true,
    val offUsername: String = ""
)

sealed class BackupImportResult {
    data class Success(val historyCount: Int, val favoritesCount: Int) : BackupImportResult()
    data class Failure(val message: String) : BackupImportResult()
}

object BackupManager {

    private val gson = Gson()

    suspend fun exportJson(context: Context): String {
        val db = AppDatabase.getInstance(context)
        val settings = SettingsRepository(context)

        val backup = BackupData(
            history = db.scanHistoryDao().getAllOnce(),
            favorites = db.favoriteDao().getAllOnce(),
            basket = db.basketDao().getAllOnce(),
            dietaryPreferences = settings.dietaryPreferences.first().toList(),
            avoidedAllergens = settings.avoidedAllergens.first().toList(),
            themeMode = settings.themeMode.first().name,
            onboardingDone = settings.onboardingDone.first(),
            offUsername = settings.offUsername.first()
        )
        return gson.toJson(backup)
    }

    /**
     * Imports a previously exported JSON backup. Favorites and basket items
     * are matched by barcode and overwritten (no duplicates). History
     * entries are appended — re-importing the same backup file twice will
     * add its history rows again, since each scan is meant to be its own
     * timestamped event; this is a rare, low-cost edge case for a personal
     * backup feature.
     */
    suspend fun importJson(context: Context, json: String): BackupImportResult {
        val backup = try {
            gson.fromJson(json, BackupData::class.java) ?: return BackupImportResult.Failure("empty_file")
        } catch (e: Exception) {
            return BackupImportResult.Failure(e.message ?: "invalid_json")
        }

        return try {
            val db = AppDatabase.getInstance(context)
            val settings = SettingsRepository(context)

            backup.history.forEach { entity ->
                db.scanHistoryDao().insert(entity.copy(id = 0)) // id=0 lets Room assign a fresh local id
            }
            backup.favorites.forEach { entity -> db.favoriteDao().insert(entity) }
            backup.basket.forEach { entity -> db.basketDao().insert(entity) }

            settings.setDietaryPreferences(backup.dietaryPreferences.toSet())
            settings.setAvoidedAllergens(backup.avoidedAllergens.toSet())
            settings.setOnboardingDone(backup.onboardingDone)
            val themeMode = runCatching { ThemeMode.valueOf(backup.themeMode) }.getOrDefault(ThemeMode.SYSTEM)
            settings.setThemeMode(themeMode)
            if (backup.offUsername.isNotBlank()) {
                // Password intentionally left blank — never stored in the backup file.
                settings.setOffCredentials(backup.offUsername, "")
            }

            BackupImportResult.Success(
                historyCount = backup.history.size,
                favoritesCount = backup.favorites.size
            )
        } catch (e: Exception) {
            BackupImportResult.Failure(e.message ?: "unknown_error")
        }
    }
}
