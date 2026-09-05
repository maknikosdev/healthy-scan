package com.healthyscan.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.healthyscan.app.data.local.AppDatabase
import com.healthyscan.app.data.local.BasketItemEntity
import com.healthyscan.app.data.local.FavoriteEntity
import com.healthyscan.app.data.local.ScanHistoryEntity
import com.healthyscan.app.data.model.HealthScoreResult
import com.healthyscan.app.data.model.Product
import com.healthyscan.app.data.remote.ProductMapper
import com.healthyscan.app.data.remote.RetrofitClient
import com.healthyscan.app.scoring.HealthScoreEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed class ProductLookupResult {
    data class Found(val product: Product, val score: HealthScoreResult) : ProductLookupResult()
    object NotFound : ProductLookupResult()
    data class Error(val message: String) : ProductLookupResult()
}

class ProductRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val gson = Gson()

    suspend fun lookupByBarcode(barcode: String): ProductLookupResult = try {
        val response = RetrofitClient.openFoodFactsApi.getProduct(barcode)
        val offProduct = response.product
        if (response.status != 1 || offProduct == null) {
            ProductLookupResult.NotFound
        } else {
            val product = ProductMapper.map(offProduct).copy(barcode = barcode)
            val score = HealthScoreEngine.score(product)
            recordScan(product, score)
            ProductLookupResult.Found(product, score)
        }
    } catch (e: Exception) {
        ProductLookupResult.Error(e.message ?: "unknown_error")
    }

    suspend fun searchProducts(query: String): List<Product> = try {
        RetrofitClient.openFoodFactsApi.searchProducts(query).products.map { ProductMapper.map(it) }
    } catch (e: Exception) {
        emptyList()
    }

    private suspend fun recordScan(product: Product, score: HealthScoreResult) {
        db.scanHistoryDao().insert(
            ScanHistoryEntity(
                barcode = product.barcode,
                name = product.name,
                brand = product.brand,
                imageUrl = product.imageUrl,
                score = score.score,
                timestampMillis = System.currentTimeMillis(),
                productJson = gson.toJson(product)
            )
        )
    }

    fun recentScans(limit: Int = 10): Flow<List<ScanHistoryEntity>> =
        db.scanHistoryDao().observeRecent(limit)

    fun allHistory(): Flow<List<ScanHistoryEntity>> = db.scanHistoryDao().observeAll()

    suspend fun totalScanned(): Int = db.scanHistoryDao().totalScanned()

    suspend fun averageScoreLast7Days(): Int {
        val since = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        return (db.scanHistoryDao().averageScoreSince(since) ?: 0.0).toInt()
    }

    // --- Favorites ---

    fun favorites(): Flow<List<FavoriteEntity>> = db.favoriteDao().observeAll()

    fun isFavorite(barcode: String): Flow<Boolean> = db.favoriteDao().isFavorite(barcode)

    suspend fun toggleFavorite(product: Product, score: Int, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            db.favoriteDao().deleteByBarcode(product.barcode)
        } else {
            db.favoriteDao().insert(
                FavoriteEntity(
                    barcode = product.barcode,
                    name = product.name,
                    brand = product.brand,
                    imageUrl = product.imageUrl,
                    score = score,
                    addedAtMillis = System.currentTimeMillis(),
                    productJson = gson.toJson(product)
                )
            )
        }
    }

    // --- Basket ---

    fun basketItems(): Flow<List<BasketItemEntity>> = db.basketDao().observeAll()

    val basketOverallScore: Flow<Int> = db.basketDao().observeAll().map { items ->
        if (items.isEmpty()) 0 else items.map { it.score }.average().toInt()
    }

    suspend fun addToBasket(product: Product, score: Int) {
        db.basketDao().insert(
            BasketItemEntity(
                barcode = product.barcode,
                name = product.name,
                imageUrl = product.imageUrl,
                score = score,
                addedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeFromBasket(barcode: String) = db.basketDao().deleteByBarcode(barcode)
}
