package com.healthyscan.app.scoring

import com.healthyscan.app.data.model.*
import kotlin.math.roundToInt

/**
 * Health Scoring Engine
 * =====================
 * Turns raw nutrition data (per 100g) into a single 0-100 Health Score, plus a
 * transparent breakdown of which factors pushed the score up or down.
 *
 * Design goals (matching the product spec):
 *  - Transparent: every point deduction/addition maps to a named factor.
 *  - Category-aware: a cereal, a cheese and a soft drink are judged against
 *    different thresholds (see [thresholdsFor]).
 *  - NOT a medical verdict: this is a nutritional-quality heuristic loosely
 *    inspired by published models such as Nutri-Score, not a diagnosis.
 *
 * This is a deliberately simple, fully offline heuristic so the app works
 * without any paid AI/nutrition API. Swap [score] out for a call to your own
 * backend or a more sophisticated model whenever you're ready — the rest of
 * the app only depends on [HealthScoreResult].
 */
object HealthScoreEngine {

    private data class Thresholds(
        val sugarHigh: Double, val sugarModerate: Double,
        val satFatHigh: Double, val satFatModerate: Double,
        val saltHigh: Double, val saltModerate: Double,
        val fiberGood: Double, val fiberModerate: Double,
        val proteinGood: Double, val proteinModerate: Double
    )

    private fun thresholdsFor(category: ProductCategory): Thresholds = when (category) {
        ProductCategory.BEVERAGE -> Thresholds(
            sugarHigh = 8.0, sugarModerate = 3.0,
            satFatHigh = 1.5, satFatModerate = 0.5,
            saltHigh = 0.3, saltModerate = 0.1,
            fiberGood = 1.5, fiberModerate = 0.5,
            proteinGood = 3.0, proteinModerate = 1.0
        )
        ProductCategory.DAIRY -> Thresholds(
            sugarHigh = 12.0, sugarModerate = 5.0,
            satFatHigh = 5.0, satFatModerate = 2.5,
            saltHigh = 1.0, saltModerate = 0.5,
            fiberGood = 2.0, fiberModerate = 0.5,
            proteinGood = 6.0, proteinModerate = 3.0
        )
        ProductCategory.SNACK_AND_CONFECTIONERY -> Thresholds(
            sugarHigh = 30.0, sugarModerate = 15.0,
            satFatHigh = 8.0, satFatModerate = 4.0,
            saltHigh = 1.2, saltModerate = 0.6,
            fiberGood = 4.0, fiberModerate = 2.0,
            proteinGood = 8.0, proteinModerate = 4.0
        )
        ProductCategory.CEREAL_AND_GRAINS -> Thresholds(
            sugarHigh = 20.0, sugarModerate = 10.0,
            satFatHigh = 4.0, satFatModerate = 1.5,
            saltHigh = 1.2, saltModerate = 0.6,
            fiberGood = 6.0, fiberModerate = 3.0,
            proteinGood = 8.0, proteinModerate = 4.0
        )
        ProductCategory.MEAT_AND_FISH -> Thresholds(
            sugarHigh = 5.0, sugarModerate = 2.0,
            satFatHigh = 6.0, satFatModerate = 3.0,
            saltHigh = 1.5, saltModerate = 0.8,
            fiberGood = 2.0, fiberModerate = 0.5,
            proteinGood = 18.0, proteinModerate = 10.0
        )
        ProductCategory.FATS_AND_OILS -> Thresholds(
            sugarHigh = 5.0, sugarModerate = 2.0,
            satFatHigh = 20.0, satFatModerate = 10.0,
            saltHigh = 1.5, saltModerate = 0.8,
            fiberGood = 2.0, fiberModerate = 0.5,
            proteinGood = 6.0, proteinModerate = 2.0
        )
        ProductCategory.FRUIT_AND_VEGETABLE -> Thresholds(
            sugarHigh = 15.0, sugarModerate = 8.0,
            satFatHigh = 2.0, satFatModerate = 0.5,
            saltHigh = 0.5, saltModerate = 0.2,
            fiberGood = 3.0, fiberModerate = 1.5,
            proteinGood = 4.0, proteinModerate = 1.5
        )
        ProductCategory.PREPARED_MEAL -> Thresholds(
            sugarHigh = 10.0, sugarModerate = 5.0,
            satFatHigh = 5.0, satFatModerate = 2.5,
            saltHigh = 1.5, saltModerate = 0.8,
            fiberGood = 4.0, fiberModerate = 2.0,
            proteinGood = 10.0, proteinModerate = 5.0
        )
        ProductCategory.GENERAL -> Thresholds(
            sugarHigh = 15.0, sugarModerate = 7.5,
            satFatHigh = 5.0, satFatModerate = 2.0,
            saltHigh = 1.2, saltModerate = 0.6,
            fiberGood = 3.0, fiberModerate = 1.5,
            proteinGood = 8.0, proteinModerate = 4.0
        )
    }

    fun score(product: Product): HealthScoreResult {
        val n = product.nutrition
        val t = thresholdsFor(product.category)

        var points = 100.0
        val factors = mutableListOf<FactorBreakdown>()
        val positives = mutableListOf<String>()
        val watchOuts = mutableListOf<String>()

        // --- Sugar ---
        val sugarLevel = when {
            n.sugarsGrams >= t.sugarHigh -> NutrientLevel.HIGH
            n.sugarsGrams >= t.sugarModerate -> NutrientLevel.MODERATE
            else -> NutrientLevel.LOW
        }
        points -= when (sugarLevel) {
            NutrientLevel.HIGH -> 22.0
            NutrientLevel.MODERATE -> 10.0
            else -> 0.0
        }
        factors += FactorBreakdown("sugar", sugarLevel)
        if (sugarLevel == NutrientLevel.HIGH) watchOuts += "high_sugar" else if (sugarLevel == NutrientLevel.LOW) positives += "low_sugar"

        // --- Saturated fat ---
        val satFatLevel = when {
            n.saturatedFatGrams >= t.satFatHigh -> NutrientLevel.HIGH
            n.saturatedFatGrams >= t.satFatModerate -> NutrientLevel.MODERATE
            else -> NutrientLevel.LOW
        }
        points -= when (satFatLevel) {
            NutrientLevel.HIGH -> 18.0
            NutrientLevel.MODERATE -> 8.0
            else -> 0.0
        }
        factors += FactorBreakdown("saturated_fat", satFatLevel)
        if (satFatLevel == NutrientLevel.HIGH) watchOuts += "high_saturated_fat" else if (satFatLevel == NutrientLevel.LOW) positives += "low_saturated_fat"

        // --- Salt ---
        val saltLevel = when {
            n.saltGrams >= t.saltHigh -> NutrientLevel.HIGH
            n.saltGrams >= t.saltModerate -> NutrientLevel.MODERATE
            else -> NutrientLevel.LOW
        }
        points -= when (saltLevel) {
            NutrientLevel.HIGH -> 15.0
            NutrientLevel.MODERATE -> 6.0
            else -> 0.0
        }
        factors += FactorBreakdown("salt", saltLevel)
        if (saltLevel == NutrientLevel.HIGH) watchOuts += "high_salt" else if (saltLevel == NutrientLevel.LOW) positives += "low_salt"

        // --- Fiber (bonus) ---
        val fiberLevel = when {
            n.fiberGrams >= t.fiberGood -> NutrientLevel.GOOD
            n.fiberGrams >= t.fiberModerate -> NutrientLevel.MODERATE
            else -> NutrientLevel.LOW
        }
        points += when (fiberLevel) {
            NutrientLevel.GOOD -> 10.0
            NutrientLevel.MODERATE -> 4.0
            else -> 0.0
        }
        factors += FactorBreakdown("fiber", fiberLevel)
        if (fiberLevel == NutrientLevel.GOOD) positives += "good_fiber"

        // --- Protein (bonus) ---
        val proteinLevel = when {
            n.proteinGrams >= t.proteinGood -> NutrientLevel.GOOD
            n.proteinGrams >= t.proteinModerate -> NutrientLevel.MODERATE
            else -> NutrientLevel.LOW
        }
        points += when (proteinLevel) {
            NutrientLevel.GOOD -> 8.0
            NutrientLevel.MODERATE -> 3.0
            else -> 0.0
        }
        factors += FactorBreakdown("protein", proteinLevel)
        if (proteinLevel == NutrientLevel.GOOD) positives += "good_protein"

        // Whole-grain / additive-count nudges could be added here once
        // ingredient parsing is wired to a real database (see ProductRepository).
        if (product.additives.size >= 5) {
            points -= 5.0
            watchOuts += "many_additives"
        }

        val finalScore = points.roundToInt().coerceIn(0, 100)
        return HealthScoreResult(
            score = finalScore,
            band = ScoreBand.forScore(finalScore),
            factors = factors,
            positives = positives,
            watchOuts = watchOuts
        )
    }

    /**
     * Combines the base [HealthScoreResult] with the user's stated dietary
     * preferences (see SettingsRepository) into a personalized 0-100 score.
     * This never diagnoses anything — it only reflects the user's own stated
     * goals back at them.
     */
    fun personalize(
        base: HealthScoreResult,
        userPreferences: Set<String>
    ): PersonalizedScoreResult {
        if (userPreferences.isEmpty()) {
            return PersonalizedScoreResult(base.score, base.score >= 60, "")
        }

        var adjusted = base.score.toDouble()
        var conflictFound = false

        if ("less_sugar" in userPreferences && "high_sugar" in base.watchOuts) {
            adjusted -= 20; conflictFound = true
        }
        if ("less_salt" in userPreferences && "high_salt" in base.watchOuts) {
            adjusted -= 15; conflictFound = true
        }
        if ("less_saturated_fat" in userPreferences && "high_saturated_fat" in base.watchOuts) {
            adjusted -= 15; conflictFound = true
        }
        if ("more_protein" in userPreferences && "good_protein" in base.positives) {
            adjusted += 10
        }
        if ("more_fiber" in userPreferences && "good_fiber" in base.positives) {
            adjusted += 10
        }

        val finalScore = adjusted.roundToInt().coerceIn(0, 100)
        return PersonalizedScoreResult(
            score = finalScore,
            isGoodFit = !conflictFound && finalScore >= 60,
            message = if (conflictFound) "personalized_caution_message" else "personalized_good_message"
        )
    }
}
