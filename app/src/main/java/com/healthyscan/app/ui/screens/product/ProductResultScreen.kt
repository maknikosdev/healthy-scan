package com.healthyscan.app.ui.screens.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthyscan.app.R
import com.healthyscan.app.data.model.*
import com.healthyscan.app.data.repository.ProductLookupResult
import com.healthyscan.app.data.repository.ProductRepository
import com.healthyscan.app.locale.LocaleManager
import com.healthyscan.app.scoring.ExplanationGenerator
import com.healthyscan.app.ui.components.ScorePill
import com.healthyscan.app.ui.components.ScoreRing
import com.healthyscan.app.ui.components.colorForBand
import com.healthyscan.app.ui.components.labelForBand
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductResultScreen(
    barcode: String,
    productRepository: ProductRepository,
    avoidedAllergens: Set<String>,
    userDietaryPreferences: Set<String>,
    shouldRecordScan: Boolean,
    onBack: () -> Unit,
    onOpenAlternative: (String) -> Unit,
    onScanLabel: () -> Unit,
    onAddProduct: (name: String, brand: String) -> Unit
) {
    var state by remember { mutableStateOf<ProductLookupResult?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(barcode) {
        state = if (shouldRecordScan) {
            productRepository.lookupByBarcode(barcode)
        } else {
            productRepository.openCachedOrLookup(barcode)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                null -> LoadingSequence()
                is ProductLookupResult.Error -> ErrorState(onRetry = {
                    scope.launch {
                        state = if (shouldRecordScan) {
                            productRepository.lookupByBarcode(barcode)
                        } else {
                            productRepository.openCachedOrLookup(barcode)
                        }
                    }
                })
                is ProductLookupResult.NotFound -> UnknownProductState(
                    onScanLabel = onScanLabel,
                    onAddProduct = { onAddProduct("", "") }
                )
                is ProductLookupResult.FoundBasicInfo -> BasicInfoState(
                    product = s.product,
                    onScanLabel = onScanLabel,
                    onAddProduct = { onAddProduct(s.product.name, s.product.brand ?: "") }
                )
                is ProductLookupResult.Found -> ProductFound(
                    product = s.product,
                    score = s.score,
                    productRepository = productRepository,
                    avoidedAllergens = avoidedAllergens,
                    userDietaryPreferences = userDietaryPreferences
                )
            }
        }
    }
}

@Composable
private fun LoadingSequence() {
    val steps = listOf(
        R.string.lookup_searching_product,
        R.string.lookup_loading_nutrition,
        R.string.lookup_analyzing_ingredients,
        R.string.lookup_building_score
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(stringResource(steps.last()), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.error_generic), style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun UnknownProductState(
    onScanLabel: () -> Unit,
    onAddProduct: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.unknown_product_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.unknown_product_body),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        Button(onClick = onScanLabel, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_photograph_label))
        }
        OutlinedButton(
            onClick = onAddProduct,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.action_add_product))
        }
    }
}

@Composable
private fun BasicInfoState(
    product: Product,
    onScanLabel: () -> Unit,
    onAddProduct: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(product.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        product.brand?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
        }
        Text(
            stringResource(R.string.basic_info_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            stringResource(R.string.basic_info_body),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        Button(onClick = onScanLabel, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_photograph_label))
        }
        OutlinedButton(
            onClick = onAddProduct,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.action_add_product))
        }
    }
}

@Composable
private fun ProductFound(
    product: Product,
    score: HealthScoreResult,
    productRepository: ProductRepository,
    avoidedAllergens: Set<String>,
    userDietaryPreferences: Set<String>
) {
    val isFavorite by productRepository.isFavorite(product.barcode).collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    val language = LocaleManager.currentLanguage()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(product.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                product.brand?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
                Spacer(Modifier.height(12.dp))
                ScoreRing(score = score.score, band = score.band)
                Spacer(Modifier.height(8.dp))
                ScorePill(text = labelForBand(score.band), color = colorForBand(score.band))
            }
        }

        item {
            SectionCard(title = stringResource(R.string.why_this_score)) {
                if (score.positives.isNotEmpty()) {
                    Text(stringResource(R.string.whats_good), fontWeight = FontWeight.SemiBold)
                    score.positives.forEach { Text("• ${humanFactor(it, language)}") }
                    Spacer(Modifier.height(8.dp))
                }
                if (score.watchOuts.isNotEmpty()) {
                    Text(stringResource(R.string.whats_to_watch), fontWeight = FontWeight.SemiBold)
                    score.watchOuts.forEach { Text("• ${humanFactor(it, language)}") }
                }
            }
        }

        item {
            SectionCard(title = stringResource(R.string.ai_explain_simply)) {
                Text(stringResource(R.string.ai_explanation_intro), fontWeight = FontWeight.SemiBold)
                Text(ExplanationGenerator.generate(score, language), modifier = Modifier.padding(top = 4.dp))
            }
        }

        item {
            SectionCard(title = stringResource(R.string.nutrition_facts_title)) {
                Text(stringResource(R.string.per_100g), fontWeight = FontWeight.SemiBold)
                NutritionRow(stringResource(R.string.nutrient_energy), "${product.nutrition.energyKcal.toInt()} kcal")
                NutritionRow(stringResource(R.string.nutrient_fat), "${product.nutrition.fatGrams}g")
                NutritionRow(stringResource(R.string.nutrient_saturated), "${product.nutrition.saturatedFatGrams}g")
                NutritionRow(stringResource(R.string.nutrient_carbs), "${product.nutrition.carbohydratesGrams}g")
                NutritionRow(stringResource(R.string.nutrient_sugars), "${product.nutrition.sugarsGrams}g")
                NutritionRow(stringResource(R.string.nutrient_fiber), "${product.nutrition.fiberGrams}g")
                NutritionRow(stringResource(R.string.nutrient_protein), "${product.nutrition.proteinGrams}g")
                NutritionRow(stringResource(R.string.nutrient_salt), "${product.nutrition.saltGrams}g")
            }
        }

        if (product.ingredients.isNotEmpty()) {
            item {
                SectionCard(title = stringResource(R.string.ingredients_title)) {
                    val core = product.ingredients.filter { it.category == IngredientCategory.CORE }
                    val sweeteners = product.ingredients.filter { it.category == IngredientCategory.SWEETENER }
                    if (core.isNotEmpty()) {
                        Text(stringResource(R.string.ingredients_core), fontWeight = FontWeight.SemiBold)
                        core.forEach { Text("• ${it.name}") }
                        Spacer(Modifier.height(8.dp))
                    }
                    if (sweeteners.isNotEmpty()) {
                        Text(stringResource(R.string.ingredients_sweeteners), fontWeight = FontWeight.SemiBold)
                        sweeteners.forEach { Text("• ${it.name}") }
                    }
                }
            }
        }

        if (product.allergens.isNotEmpty()) {
            item {
                SectionCard(title = stringResource(R.string.allergens_title)) {
                    Text(stringResource(R.string.allergens_contains))
                    product.allergens.forEach { Text("• $it") }

                    val personalConflict = product.allergens.any { allergen ->
                        avoidedAllergens.any { avoided -> allergen.contains(avoided, ignoreCase = true) }
                    }
                    if (personalConflict) {
                        Spacer(Modifier.height(8.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    stringResource(R.string.allergen_personal_warning_title),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(stringResource(R.string.allergen_personal_warning_body))
                            }
                        }
                    }
                }
            }
        }

        if (product.additives.isNotEmpty()) {
            item {
                SectionCard(title = stringResource(R.string.additives_title)) {
                    product.additives.forEach { additive ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${additive.code} — ${additive.name}", fontWeight = FontWeight.SemiBold)
                                Text(additive.roleInProduct, style = MaterialTheme.typography.bodyMedium)
                            }
                            ScorePill(
                                text = additive.assessment.name,
                                color = colorForBand(ScoreBand.forScore(levelToApproxScore(additive.assessment)))
                            )
                        }
                    }
                }
            }
        }

        item {
            val personalized = com.healthyscan.app.scoring.HealthScoreEngine.personalize(score, userDietaryPreferences)
            SectionCard(title = stringResource(R.string.is_it_good_for_me)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScorePill(
                        text = stringResource(R.string.score_for_you, personalized.score),
                        color = colorForBand(ScoreBand.forScore(personalized.score))
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (personalized.isGoodFit) stringResource(R.string.personalized_good_message)
                    else stringResource(R.string.personalized_caution_message)
                )
            }
        }

        item {
            Button(
                onClick = {
                    scope.launch { productRepository.toggleFavorite(product, score.score, isFavorite) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.save_to_favorites))
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun NutritionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun levelToApproxScore(level: NutrientLevel): Int = when (level) {
    NutrientLevel.GOOD -> 90
    NutrientLevel.LOW -> 80
    NutrientLevel.MODERATE -> 65
    NutrientLevel.HIGH -> 40
}

private fun humanFactor(key: String, language: String): String {
    val isGreek = language == "el"
    return when (key) {
        "low_sugar" -> if (isGreek) "Χαμηλή ζάχαρη" else "Low sugar"
        "low_saturated_fat" -> if (isGreek) "Χαμηλά κορεσμένα λιπαρά" else "Low saturated fat"
        "low_salt" -> if (isGreek) "Χαμηλό αλάτι" else "Low salt"
        "good_fiber" -> if (isGreek) "Καλή ποσότητα φυτικών ινών" else "Good amount of fiber"
        "good_protein" -> if (isGreek) "Καλή ποσότητα πρωτεΐνης" else "Good amount of protein"
        "high_sugar" -> if (isGreek) "Υψηλή περιεκτικότητα σε σάκχαρα" else "High sugar content"
        "high_saturated_fat" -> if (isGreek) "Υψηλά κορεσμένα λιπαρά" else "High saturated fat"
        "high_salt" -> if (isGreek) "Υψηλή περιεκτικότητα σε αλάτι" else "High salt content"
        "many_additives" -> if (isGreek) "Αρκετά πρόσθετα" else "Quite a few additives"
        else -> key
    }
}
