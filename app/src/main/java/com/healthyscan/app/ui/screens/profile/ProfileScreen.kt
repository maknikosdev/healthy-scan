package com.healthyscan.app.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthyscan.app.R
import com.healthyscan.app.data.backup.BackupImportResult
import com.healthyscan.app.data.backup.BackupManager
import com.healthyscan.app.data.repository.ProductRepository
import com.healthyscan.app.data.repository.SettingsRepository
import com.healthyscan.app.ui.components.HealthyScanTopBar
import com.healthyscan.app.ui.screens.onboarding.allAllergenOptions
import com.healthyscan.app.ui.screens.onboarding.allPreferenceOptions
import com.healthyscan.app.ui.theme.ThemeMode
import kotlinx.coroutines.launch

private enum class BackupStatus { NONE, EXPORT_SUCCESS, EXPORT_ERROR, IMPORTING, IMPORT_ERROR }

@Composable
fun ProfileScreen(
    productRepository: ProductRepository,
    settingsRepository: SettingsRepository,
    isDarkMode: Boolean,
    currentLanguage: String,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenPremium: () -> Unit,
    onEditPreferences: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dietaryPreferences by settingsRepository.dietaryPreferences.collectAsState(initial = emptySet())
    val avoidedAllergens by settingsRepository.avoidedAllergens.collectAsState(initial = emptySet())
    val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

    var totalScanned by remember { mutableIntStateOf(0) }
    var avgScore by remember { mutableIntStateOf(0) }

    var backupStatus by remember { mutableStateOf(BackupStatus.NONE) }
    var importMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        totalScanned = productRepository.totalScanned()
        avgScore = productRepository.averageScore()
    }

    // Lets the person choose WHERE to save the backup file (Downloads, a
    // cloud-synced folder, etc.) — no storage permission needed.
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = BackupManager.exportJson(context)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                backupStatus = BackupStatus.EXPORT_SUCCESS
            } catch (e: Exception) {
                backupStatus = BackupStatus.EXPORT_ERROR
            }
        }
    }

    // Lets the person pick the .json file they exported earlier (from this
    // device or a different one) to bring their data across.
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        backupStatus = BackupStatus.IMPORTING
        scope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.readBytes().toString(Charsets.UTF_8)
                }
                if (json == null) {
                    backupStatus = BackupStatus.IMPORT_ERROR
                    return@launch
                }
                when (val result = BackupManager.importJson(context, json)) {
                    is BackupImportResult.Success -> {
                        importMessage = "${result.historyCount}|${result.favoritesCount}"
                        backupStatus = BackupStatus.NONE // success message shown via importMessage below
                        totalScanned = productRepository.totalScanned()
                        avgScore = productRepository.averageScore()
                    }
                    is BackupImportResult.Failure -> {
                        backupStatus = BackupStatus.IMPORT_ERROR
                    }
                }
            } catch (e: Exception) {
                backupStatus = BackupStatus.IMPORT_ERROR
            }
        }
    }

    Scaffold(
        topBar = {
            HealthyScanTopBar(
                title = stringResource(R.string.profile_title),
                isDarkMode = isDarkMode,
                currentLanguage = currentLanguage,
                onToggleTheme = onToggleTheme,
                onToggleLanguage = onToggleLanguage
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                ProfileSection(title = stringResource(R.string.profile_my_stats)) {
                    Text(stringResource(R.string.profile_products_scanned, totalScanned))
                    Text(stringResource(R.string.profile_avg_score, avgScore))
                }
            }

            item {
                ProfileSection(title = stringResource(R.string.profile_my_preferences)) {
                    if (dietaryPreferences.isEmpty()) {
                        Text(stringResource(R.string.favorites_empty))
                    } else {
                        val labels = allPreferenceOptions.filter { it.key in dietaryPreferences }
                        labels.forEach { option ->
                            Text("• ${stringResource(option.labelRes)}")
                        }
                    }

                    Text(
                        stringResource(R.string.profile_my_allergens),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    if (avoidedAllergens.isEmpty()) {
                        Text(stringResource(R.string.favorites_empty))
                    } else {
                        val allergenLabels = allAllergenOptions.filter { it.key in avoidedAllergens }
                        allergenLabels.forEach { option ->
                            Text("• ${stringResource(option.labelRes)}")
                        }
                    }

                    OutlinedButton(
                        onClick = onEditPreferences,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(stringResource(R.string.profile_edit_preferences))
                    }
                }
            }

            item {
                ProfileSection(title = stringResource(R.string.profile_settings)) {
                    Text(stringResource(R.string.profile_theme), fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)) {
                        ThemeChoiceButton(ThemeMode.SYSTEM, themeMode, stringResource(R.string.theme_system)) {
                            scope.launch { settingsRepository.setThemeMode(ThemeMode.SYSTEM) }
                        }
                        ThemeChoiceButton(ThemeMode.LIGHT, themeMode, stringResource(R.string.theme_light)) {
                            scope.launch { settingsRepository.setThemeMode(ThemeMode.LIGHT) }
                        }
                        ThemeChoiceButton(ThemeMode.DARK, themeMode, stringResource(R.string.theme_dark)) {
                            scope.launch { settingsRepository.setThemeMode(ThemeMode.DARK) }
                        }
                    }
                    Text(stringResource(R.string.profile_language), fontWeight = FontWeight.SemiBold)
                    OutlinedButton(onClick = onToggleLanguage, modifier = Modifier.padding(top = 4.dp)) {
                        Text(if (currentLanguage == "el") "Ελληνικά ⇄ English" else "English ⇄ Ελληνικά")
                    }
                }
            }

            item {
                ProfileSection(title = stringResource(R.string.profile_backup_title)) {
                    Text(
                        stringResource(R.string.profile_backup_body),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (backupStatus == BackupStatus.IMPORTING) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = { exportLauncher.launch("healthyscan_backup.json") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.profile_backup_export))
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(stringResource(R.string.profile_backup_import))
                        }
                    }

                    when (backupStatus) {
                        BackupStatus.EXPORT_SUCCESS -> Text(
                            stringResource(R.string.profile_backup_export_success),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        BackupStatus.EXPORT_ERROR -> Text(
                            stringResource(R.string.profile_backup_export_error),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        BackupStatus.IMPORT_ERROR -> Text(
                            stringResource(R.string.profile_backup_import_error),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        else -> {}
                    }

                    if (importMessage.isNotBlank()) {
                        val parts = importMessage.split("|")
                        val historyCount = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val favoritesCount = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        Text(
                            stringResource(R.string.profile_backup_import_success, historyCount, favoritesCount),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    onClick = onOpenPremium,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.premium_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            stringResource(R.string.premium_paid_3),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeChoiceButton(mode: ThemeMode, current: ThemeMode, label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.padding(end = 8.dp)) {
        Text(if (mode == current) "✓ $label" else label)
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
