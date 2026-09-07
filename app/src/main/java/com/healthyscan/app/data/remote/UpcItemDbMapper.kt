package com.healthyscan.app.data.remote

import com.healthyscan.app.data.model.NutritionFacts
import com.healthyscan.app.data.model.Product

object UpcItemDbMapper {

    private val emptyNutrition = NutritionFacts(
        energyKcal = 0.0, fatGrams = 0.0, saturatedFatGrams = 0.0,
        carbohydratesGrams = 0.0, sugarsGrams = 0.0, fiberGrams = 0.0,
        proteinGrams = 0.0, saltGrams = 0.0
    )

    /**
     * Builds an identification-only [Product] from a UPCitemdb item. Never
     * has nutrition/ingredients — [Product.hasNutritionData] is false, and
     * callers must skip Health Score computation for it.
     */
    fun map(barcode: String, item: UpcItemDbItem): Product = Product(
        barcode = barcode,
        name = item.title?.takeIf { it.isNotBlank() } ?: "Unknown product",
        brand = item.brand,
        imageUrl = item.images?.firstOrNull(),
        category = ProductMapper.classifyCategory(listOfNotNull(item.category)),
        servingSizeGrams = null,
        nutrition = emptyNutrition,
        ingredientsText = null,
        hasNutritionData = false
    )
}
