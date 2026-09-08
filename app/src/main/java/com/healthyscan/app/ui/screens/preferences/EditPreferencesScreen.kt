package com.healthyscan.app.ui.screens.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthyscan.app.R
import com.healthyscan.app.data.repository.SettingsRepository
import com.healthyscan.app.ui.screens.onboarding.CheckboxRow
import com.healthyscan.app.ui.screens.onboarding.allAllergenOptions
import com.healthyscan.app.ui.screens.onboarding.allPreferenceOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPreferencesScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val savedPreferences by settingsRepository.dietaryPreferences.collectAsState(initial = emptySet())
    val savedAllergens by settingsRepository.avoidedAllergens.collectAsState(initial = emptySet())

    var selectedPreferences by remember { mutableStateOf<Set<String>?>(null) }
    var selectedAllergens by remember { mutableStateOf<Set<String>?>(null) }
    var justSaved by remember { mutableStateOf(false) }

    // Seed local editable state from the saved values exactly once they arrive.
    LaunchedEffect(savedPreferences, savedAllergens) {
        if (selectedPreferences == null) selectedPreferences = savedPreferences
        if (selectedAllergens == null) selectedAllergens = savedAllergens
    }

    val preferences = selectedPreferences ?: emptySet()
    val allergens = selectedAllergens ?: emptySet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_preferences_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.onboarding_question),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(allPreferenceOptions) { option ->
                CheckboxRow(
                    checked = option.key in preferences,
                    label = stringResource(option.labelRes),
                    onToggle = { checked ->
                        selectedPreferences = if (checked) preferences + option.key else preferences - option.key
                    }
                )
            }

            item {
                Text(
                    text = stringResource(R.string.onboarding_allergens_question),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
                )
                Text(
                    text = stringResource(R.string.onboarding_allergens_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(allAllergenOptions) { option ->
                CheckboxRow(
                    checked = option.key in allergens,
                    label = stringResource(option.labelRes),
                    onToggle = { checked ->
                        selectedAllergens = if (checked) allergens + option.key else allergens - option.key
                    }
                )
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            settingsRepository.setDietaryPreferences(preferences)
                            settingsRepository.setAvoidedAllergens(allergens)
                            justSaved = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(stringResource(R.string.edit_preferences_save))
                }
                if (justSaved) {
                    Text(
                        stringResource(R.string.edit_preferences_saved),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
