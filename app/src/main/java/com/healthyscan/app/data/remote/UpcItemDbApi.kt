package com.healthyscan.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * UPCitemdb (https://www.upcitemdb.com) is a general-purpose barcode/UPC
 * database. Its free "trial" tier needs no API key and allows up to 100
 * lookups per day per IP — plenty for an app used by one person, and it
 * degrades gracefully (just returns nothing) if the daily cap is hit.
 *
 * It does NOT have ingredients or nutrition data — only identification
 * fields (title, brand, category, images). It's used for two things:
 *   1. As a fallback when Open Food Facts has never heard of the barcode,
 *      so the person at least sees what they scanned instead of a dead end.
 *   2. To fill in a missing product photo when Open Food Facts has the
 *      nutrition data but no image.
 */
interface UpcItemDbApi {

    @GET("prod/trial/lookup")
    suspend fun lookup(@Query("upc") upc: String): UpcItemDbResponse
}

data class UpcItemDbResponse(
    @SerializedName("code") val code: String?,
    @SerializedName("total") val total: Int?,
    @SerializedName("items") val items: List<UpcItemDbItem>?
)

data class UpcItemDbItem(
    @SerializedName("title") val title: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("images") val images: List<String>?
)
