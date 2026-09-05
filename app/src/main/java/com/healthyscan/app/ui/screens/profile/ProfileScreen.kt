package com.healthyscan.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthyscan.app.R
import com.healthyscan.app.data.repository.ProductRepository
import com.healthyscan.app.data.repository.SettingsRepository
import com.healthyscan.app.ui.components.HealthyScanTopBar
import com.healthyscan.app.ui.screens.onboarding.allPreferenceOptions
import com.healthyscan.app.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    productRepository: ProductRepository,
    settingsRepository: SettingsRepository,
    isDarkMode: Boolean,
    currentLanguage: String,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenPremium: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val dietaryPreferences by settingsRepository.dietaryPreferences.collectAsState(initial = emptySet())
    val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

    var totalScanned by remember { mutableIntStateOf(0) }
    var avgScore by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        totalScanned = productRepository.totalScanned()
        avgScore = productRepository.averageScoreLast7Days()
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
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
