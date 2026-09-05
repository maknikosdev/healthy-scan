package com.healthyscan.app.scoring

import com.healthyscan.app.data.model.HealthScoreResult

/**
 * Produces the "🤖 Explain it simply" text shown on the product result screen.
 *
 * This is a deliberately simple, fully offline, rule-based generator built
 * from the same factors as [HealthScoreEngine] — no network call, no API key,
 * works instantly and for free.
 *
 * To upgrade this to a real LLM-generated explanation:
 *   1. Stand up a small backend endpoint (Cloud Run / Lambda / etc.) that
 *      holds your Anthropic API key server-side — NEVER embed an API key
 *      directly in the Android app, it can be extracted from the APK.
 *   2. Have that endpoint call the Claude API (model "claude-sonnet-4-6" or
 *      newer) with the product's nutrition facts + [HealthScoreResult] and
 *      return a short plain-language paragraph.
 *   3. Replace the body of [generate] with a Retrofit call to your endpoint,
 *      keeping the same function signature so the UI layer doesn't change.
 */
object ExplanationGenerator {

    fun generate(result: HealthScoreResult, language: String): String {
        val isGreek = language == "el"

        val positivesText = result.positives.joinToString(", ") { key -> label(key, isGreek) }
        val watchOutsText = result.watchOuts.joinToString(", ") { key -> label(key, isGreek) }

        return if (isGreek) {
            buildString {
                if (result.positives.isNotEmpty()) {
                    append("Το προϊόν έχει ορισμένα θετικά χαρακτηριστικά, όπως $positivesText. ")
                }
                if (result.watchOuts.isNotEmpty()) {
                    append("Ωστόσο, χρειάζεται προσοχή σε: $watchOutsText. ")
                }
                if (result.score >= 75) {
                    append("Συνολικά, πρόκειται για μια αρκετά καλή επιλογή.")
                } else if (result.score >= 60) {
                    append("Αν ψάχνεις κάτι για συχνή κατανάλωση, μπορείς να εξετάσεις εναλλακτικές με καλύτερο προφίλ.")
                } else {
                    append("Αν το καταναλώνεις τακτικά, ίσως αξίζει να δεις τις προτεινόμενες εναλλακτικές.")
                }
            }
        } else {
            buildString {
                if (result.positives.isNotEmpty()) {
                    append("This product has some positive traits, such as $positivesText. ")
                }
                if (result.watchOuts.isNotEmpty()) {
                    append("However, it's worth watching: $watchOutsText. ")
                }
                if (result.score >= 75) {
                    append("Overall, it's a fairly good choice.")
                } else if (result.score >= 60) {
                    append("If you're looking for something to eat often, you might consider the alternatives below.")
                } else {
                    append("If you consume this regularly, it may be worth checking the suggested alternatives.")
                }
            }
        }
    }

    private fun label(key: String, isGreek: Boolean): String = when (key) {
        "low_sugar" -> if (isGreek) "χαμηλή ζάχαρη" else "low sugar"
        "low_saturated_fat" -> if (isGreek) "χαμηλά κορεσμένα λιπαρά" else "low saturated fat"
        "low_salt" -> if (isGreek) "χαμηλό αλάτι" else "low salt"
        "good_fiber" -> if (isGreek) "καλή ποσότητα φυτικών ινών" else "good fiber content"
        "good_protein" -> if (isGreek) "καλή ποσότητα πρωτεΐνης" else "good protein content"
        "high_sugar" -> if (isGreek) "υψηλή περιεκτικότητα σε σάκχαρα" else "high sugar content"
        "high_saturated_fat" -> if (isGreek) "υψηλά κορεσμένα λιπαρά" else "high saturated fat"
        "high_salt" -> if (isGreek) "υψηλό αλάτι" else "high salt content"
        "many_additives" -> if (isGreek) "αρκετά πρόσθετα" else "quite a few additives"
        else -> key
    }
}
