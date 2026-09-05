package com.healthyscan.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Open Food Facts (https://world.openfoodfacts.org) is a free, open, crowdsourced
 * food-products database with no API key required. It's used here as the default
 * barcode lookup source. Swap this out (or add a second source) for regional
 * product coverage if needed.
 */
interface OpenFoodFactsApi {

    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = FIELDS
    ): OffProductResponse

    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): OffSearchResponse

    companion object {
        private const val FIELDS = "code,product_name,brands,image_url,image_front_url," +
            "categories_tags,serving_size,serving_quantity,ingredients_text," +
            "ingredients,additives_tags,allergens_tags,nutriments"
    }
}

data class OffProductResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("product") val product: OffProduct?
)

data class OffSearchResponse(
    @SerializedName("products") val products: List<OffProduct> = emptyList()
)

data class OffProduct(
    @SerializedName("code") val code: String?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_front_url") val imageFrontUrl: String?,
    @SerializedName("categories_tags") val categoriesTags: List<String>?,
    @SerializedName("serving_size") val servingSize: String?,
    @SerializedName("serving_quantity") val servingQuantity: Double?,
    @SerializedName("ingredients_text") val ingredientsText: String?,
    @SerializedName("ingredients") val ingredients: List<OffIngredient>?,
    @SerializedName("additives_tags") val additivesTags: List<String>?,
    @SerializedName("allergens_tags") val allergensTags: List<String>?,
    @SerializedName("nutriments") val nutriments: OffNutriments?
)

data class OffIngredient(
    @SerializedName("id") val id: String?,
    @SerializedName("text") val text: String?
)

data class OffNutriments(
    @SerializedName("energy-kcal_100g") val energyKcal100g: Double?,
    @SerializedName("fat_100g") val fat100g: Double?,
    @SerializedName("saturated-fat_100g") val saturatedFat100g: Double?,
    @SerializedName("carbohydrates_100g") val carbohydrates100g: Double?,
    @SerializedName("sugars_100g") val sugars100g: Double?,
    @SerializedName("fiber_100g") val fiber100g: Double?,
    @SerializedName("proteins_100g") val proteins100g: Double?,
    @SerializedName("salt_100g") val salt100g: Double?
)
