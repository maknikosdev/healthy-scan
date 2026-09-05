package com.healthyscan.app.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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

@Composable
fun OnboardingScreen(
    onFinished: (Set<String>) -> Unit
) {
    val selected = remember { mutableStateOf(setOf<String>()) }

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
            Text(
                text = stringResource(R.string.onboarding_question),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(allPreferenceOptions) { option ->
                    val isChecked = option.key in selected.value
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                selected.value = if (checked) {
                                    selected.value + option.key
                                } else {
                                    selected.value - option.key
                                }
                            }
                        )
                        Text(stringResource(option.labelRes), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Button(
                onClick = { onFinished(selected.value) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_continue))
            }
            TextButton(
                onClick = { onFinished(emptySet()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_skip))
            }
        }
    }
}