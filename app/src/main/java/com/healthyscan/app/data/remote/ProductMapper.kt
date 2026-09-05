package com.healthyscan.app.data.remote

import com.healthyscan.app.data.model.*

object ProductMapper {

    fun map(off: OffProduct): Product {
        val n = off.nutriments
        val nutrition = NutritionFacts(
            energyKcal = n?.energyKcal100g ?: 0.0,
            fatGrams = n?.fat100g ?: 0.0,
            saturatedFatGrams = n?.saturatedFat100g ?: 0.0,
            carbohydratesGrams = n?.carbohydrates100g ?: 0.0,
            sugarsGrams = n?.sugars100g ?: 0.0,
            fiberGrams = n?.fiber100g ?: 0.0,
            proteinGrams = n?.proteins100g ?: 0.0,
            saltGrams = n?.salt100g ?: 0.0
        )

        val ingredients = off.ingredients?.mapNotNull { it.text }
            ?.map { text -> classifyIngredient(text) }
            ?: emptyList()

        val additives = off.additivesTags.orEmpty().map { tag -> AdditiveLookup.resolve(tag) }

        val allergens = off.allergensTags.orEmpty().map { tag ->
            tag.substringAfter(":").replace('-', ' ').replaceFirstChar { it.uppercase() }
        }

        return Product(
            barcode = off.code.orEmpty(),
            name = off.productName?.takeIf { it.isNotBlank() } ?: "Unknown product",
            brand = off.brands?.split(",")?.firstOrNull()?.trim(),
            imageUrl = off.imageFrontUrl ?: off.imageUrl,
            category = classifyCategory(off.categoriesTags.orEmpty()),
            servingSizeGrams = off.servingQuantity,
            nutrition = nutrition,
            ingredientsText = off.ingredientsText,
            ingredients = ingredients,
            additives = additives,
            allergens = allergens
        )
    }

    private fun classifyIngredient(text: String): Ingredient {
        val lower = text.lowercase()
        val category = when {
            listOf("sugar", "glucose", "fructose", "syrup", "ζάχαρη", "σιρόπι").any { it in lower } ->
                IngredientCategory.SWEETENER
            lower.startsWith("e") && lower.drop(1).take(3).any { it.isDigit() } ->
                IngredientCategory.ADDITIVE
            else -> IngredientCategory.CORE
        }
        val assessment = if (category == IngredientCategory.SWEETENER) {
            NutrientLevel.HIGH
        } else {
            NutrientLevel.MODERATE
        }
        return Ingredient(name = text.trim(), category = category, assessment = assessment)
    }

    private fun classifyCategory(tags: List<String>): ProductCategory {
        val joined = tags.joinToString(" ").lowercase()
        return when {
            listOf("beverage", "drink", "soda", "juice", "water").any { it in joined } -> ProductCategory.BEVERAGE
            listOf("dairy", "milk", "yogurt", "yoghurt", "cheese").any { it in joined } -> ProductCategory.DAIRY
            listOf("cereal", "bread", "pasta", "grain", "flour").any { it in joined } -> ProductCategory.CEREAL_AND_GRAINS
            listOf("snack", "confectionery", "chocolate", "candy", "biscuit", "cookie").any { it in joined } -> ProductCategory.SNACK_AND_CONFECTIONERY
            listOf("meat", "fish", "poultry", "sausage").any { it in joined } -> ProductCategory.MEAT_AND_FISH
            listOf("oil", "fat", "butter", "margarine").any { it in joined } -> ProductCategory.FATS_AND_OILS
            listOf("fruit", "vegetable").any { it in joined } -> ProductCategory.FRUIT_AND_VEGETABLE
            listOf("meal", "dish", "ready").any { it in joined } -> ProductCategory.PREPARED_MEAL
            else -> ProductCategory.GENERAL
        }
    }
}

/**
 * Small built-in reference table for common food additives, used by the
 * "Ingredient Intelligence" feature. This is intentionally short — extend it,
 * or point it at a real additives database/API, as the product grows.
 */
object AdditiveLookup {
    private val known = mapOf(
        "e322" to Additive("E322", "Lecithin", "Emulsifier", "Helps stabilize texture", NutrientLevel.GOOD),
        "e330" to Additive("E330", "Citric acid", "Acidity regulator", "Adds tartness, helps preserve freshness", NutrientLevel.GOOD),
        "e202" to Additive("E202", "Potassium sorbate", "Preservative", "Prevents mold and yeast growth", NutrientLevel.MODERATE),
        "e211" to Additive("E211", "Sodium benzoate", "Preservative", "Prevents microbial spoilage", NutrientLevel.MODERATE),
        "e621" to Additive("E621", "Monosodium glutamate", "Flavor enhancer", "Intensifies savory taste", NutrientLevel.MODERATE),
        "e951" to Additive("E951", "Aspartame", "Artificial sweetener", "Adds sweetness without sugar's calories", NutrientLevel.MODERATE),
        "e150d" to Additive("E150d", "Caramel color (sulphite ammonia)", "Coloring", "Gives a brown/caramel color", NutrientLevel.MODERATE)
    )

    fun resolve(tag: String): Additive {
        val code = tag.substringAfterLast(":").lowercase()
        return known[code] ?: Additive(
            code = code.uppercase(),
            name = code.uppercase(),
            use = "Food additive",
            roleInProduct = "See the ingredients list for details.",
            assessment = NutrientLevel.MODERATE
        )
    }
}
