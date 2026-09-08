package com.healthyscan.app.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthyscan.app.R

data class PreferenceOption(val key: String, val labelRes: Int)

val allPreferenceOptions = listOf(
    PreferenceOption("less_sugar", R.string.pref_less_sugar),
    PreferenceOption("less_salt", R.string.pref_less_salt),
    PreferenceOption("more_protein", R.string.pref_more_protein),
    PreferenceOption("more_fiber", R.string.pref_more_fiber),
    PreferenceOption("fewer_calories", R.string.pref_fewer_calories),
    PreferenceOption("less_saturated_fat", R.string.pref_less_saturated_fat),
    PreferenceOption("simpler_composition", R.string.pref_simpler_composition),
    PreferenceOption("vegan", R.string.pref_vegan),
    PreferenceOption("gluten_free", R.string.pref_gluten_free),
    PreferenceOption("lactose_free", R.string.pref_lactose_free)
)

data class AllergenOption(val key: String, val labelRes: Int)

/**
 * Keys are matched (case-insensitive substring) against the allergen tags
 * Open Food Facts returns for a product — see ProductMapper (allergens are
 * formatted like "Milk", "Gluten", "Peanuts") and the personal-warning check
 * in ProductResultScreen. Some overlap is intentional and safety-conservative
 * (e.g. "nuts" also matches "Peanuts") — better to over-warn than miss one.
 */
val allAllergenOptions = listOf(
    AllergenOption("milk", R.string.allergen_milk),
    AllergenOption("gluten", R.string.allergen_gluten),
    AllergenOption("eggs", R.string.allergen_eggs),
    AllergenOption("peanuts", R.string.allergen_peanuts),
    AllergenOption("nuts", R.string.allergen_tree_nuts),
    AllergenOption("soy", R.string.allergen_soy),
    AllergenOption("fish", R.string.allergen_fish),
    AllergenOption("crustaceans", R.string.allergen_shellfish),
    AllergenOption("sesame", R.string.allergen_sesame),
    AllergenOption("mustard", R.string.allergen_mustard),
    AllergenOption("celery", R.string.allergen_celery),
    AllergenOption("sulphite", R.string.allergen_sulphites),
    AllergenOption("lupin", R.string.allergen_lupin),
    AllergenOption("molluscs", R.string.allergen_molluscs)
)

@Composable
fun OnboardingScreen(
    onFinished: (dietaryPreferences: Set<String>, avoidedAllergens: Set<String>) -> Unit
) {
    val selectedPreferences = remember { mutableStateOf(setOf<String>()) }
    val selectedAllergens = remember { mutableStateOf(setOf<String>()) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_welcome),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.onboarding_intro),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 4.dp)) {
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
                        checked = option.key in selectedPreferences.value,
                        label = stringResource(option.labelRes),
                        onToggle = { checked ->
                            selectedPreferences.value = if (checked) {
                                selectedPreferences.value + option.key
                            } else {
                                selectedPreferences.value - option.key
                            }
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
                        checked = option.key in selectedAllergens.value,
                        label = stringResource(option.labelRes),
                        onToggle = { checked ->
                            selectedAllergens.value = if (checked) {
                                selectedAllergens.value + option.key
                            } else {
                                selectedAllergens.value - option.key
                            }
                        }
                    )
                }
            }

            Button(
                onClick = { onFinished(selectedPreferences.value, selectedAllergens.value) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_continue))
            }
            TextButton(
                onClick = { onFinished(emptySet(), emptySet()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_skip))
            }
        }
    }
}

@Composable
fun CheckboxRow(checked: Boolean, label: String, onToggle: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = onToggle)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
