package com.healthyscan.app.data.model

enum class ScoreBand(val minInclusive: Int, val maxInclusive: Int) {
    VERY_LOW(0, 39),
    LOW(40, 59),
    MODERATE(60, 74),
    GOOD(75, 89),
    EXCELLENT(90, 100);

    companion object {
        fun forScore(score: Int): ScoreBand =
            entries.firstOrNull { score in it.minInclusive..it.maxInclusive } ?: VERY_LOW
    }
}

data class FactorBreakdown(
    val factor: String,
    val level: NutrientLevel
)

data class HealthScoreResult(
    val score: Int,
    val band: ScoreBand,
    val factors: List<FactorBreakdown>,
    val positives: List<String>,
    val watchOuts: List<String>
)

/**
 * Result of combining a [HealthScoreResult] with the user's own dietary
 * preferences (data/repository/SettingsRepository). Purely informational —
 * never presented as medical advice.
 */
data class PersonalizedScoreResult(
    val score: Int,
    val isGoodFit: Boolean,
    val message: String
)
