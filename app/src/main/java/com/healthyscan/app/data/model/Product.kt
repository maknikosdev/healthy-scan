package com.healthyscan.app.data.model

/**
 * Normalized product model used across the whole app (UI, scoring engine, database).
 * Built from the Open Food Facts API response — see data/remote/OpenFoodFactsApi.kt.
 */
data class Product(
    val barcode: String,
    val name: String,
    val brand: String?,
    val imageUrl: String?,
    val category: ProductCategory,
    val servingSizeGrams: Double?,
    val nutrition: NutritionFacts,
    val ingredientsText: String?,
    val ingredients: List<Ingredient> = emptyList(),
    val additives: List<Additive> = emptyList(),
    val allergens: List<String> = emptyList()
)

/**
 * Broad product category. The scoring engine uses this to apply different
 * thresholds — a cereal, a cheese and a soft drink should not be judged on
 * exactly the same sugar/fat/salt scale.
 */
enum class ProductCategory {
    GENERAL,
    BEVERAGE,
    DAIRY,
    CEREAL_AND_GRAINS,
    SNACK_AND_CONFECTIONERY,
    MEAT_AND_FISH,
    FATS_AND_OILS,
    FRUIT_AND_VEGETABLE,
    PREPARED_MEAL
}

/**
 * Nutrition values are per 100g/100ml, matching how packaging and Open Food
 * Facts both report values by default. UI screens compute the "per serving"
 * view from [Product.servingSizeGrams] when it's available.
 */
data class NutritionFacts(
    val energyKcal: Double,
    val fatGrams: Double,
    val saturatedFatGrams: Double,
    val carbohydratesGrams: Double,
    val sugarsGrams: Double,
    val fiberGrams: Double,
    val proteinGrams: Double,
    val saltGrams: Double
)

enum class NutrientLevel { LOW, MODERATE, HIGH, GOOD }

data class Ingredient(
    val name: String,
    val category: IngredientCategory,
    val whatIsIt: String? = null,
    val whatItMeans: String? = null,
    val assessment: NutrientLevel = NutrientLevel.MODERATE
)

enum class IngredientCategory { CORE, SWEETENER, ADDITIVE, OTHER }

data class Additive(
    val code: String,       // e.g. "E322"
    val name: String,       // e.g. "Lecithin"
    val use: String,
    val roleInProduct: String,
    val assessment: NutrientLevel
)
