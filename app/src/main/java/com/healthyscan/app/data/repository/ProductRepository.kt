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
import com.healthyscan.app.data.remote.UpcItemDbMapper
import com.healthyscan.app.scoring.HealthScoreEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed class ProductLookupResult {
    /** Full match from Open Food Facts: nutrition data available, Health Score computed. */
    data class Found(val product: Product, val score: HealthScoreResult) : ProductLookupResult()

    /** Identification-only match (from UPCitemdb): name/brand/photo known, but
     *  no nutrition data exists anywhere, so no Health Score can be shown. */
    data class FoundBasicInfo(val product: Product) : ProductLookupResult()

    object NotFound : ProductLookupResult()
    data class Error(val message: String) : ProductLookupResult()
}

sealed class ContributionResult {
    object Success : ContributionResult()
    data class Failure(val message: String) : ContributionResult()
}

class ProductRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val gson = Gson()

    /**
     * Combines two free, forever-free sources to get the most complete
     * picture possible for a scanned barcode:
     *   1. Open Food Facts — the primary source, has real nutrition,
     *      ingredients, allergens and additives when the product exists there.
     *   2. UPCitemdb — a free (100 lookups/day, no key) general barcode
     *      database used two ways: (a) as a fallback when Open Food Facts
     *      has never heard of the barcode, so the person at least sees what
     *      they scanned instead of a dead end, and (b) to fill in a missing
     *      product photo when Open Food Facts has the nutrition data but no
     *      image on file.
     */
    suspend fun lookupByBarcode(barcode: String): ProductLookupResult {
        val offResult = try {
            val response = RetrofitClient.openFoodFactsApi.getProduct(barcode)
            if (response.status == 1 && response.product != null) response.product else null
        } catch (e: Exception) {
            null
        }

        if (offResult != null) {
            var product = ProductMapper.map(offResult).copy(barcode = barcode)

            // Enrichment: if Open Food Facts has the nutrition data but is
            // missing a photo, try to fill it in from UPCitemdb (best-effort;
            // failures here are silently ignored, the core data already works).
            if (product.imageUrl.isNullOrBlank()) {
                fetchUpcItemDbImage(barcode)?.let { imageUrl ->
                    product = product.copy(imageUrl = imageUrl)
                }
            }

            val score = HealthScoreEngine.score(product)
            recordScan(product, score)
            return ProductLookupResult.Found(product, score)
        }

        // Open Food Facts doesn't know this barcode — try UPCitemdb as a
        // fallback so the person sees identification info instead of a dead end.
        val upcResult = try {
            RetrofitClient.upcItemDbApi.lookup(barcode).items?.firstOrNull()
        } catch (e: Exception) {
            null
        }

        return if (upcResult != null) {
            val product = UpcItemDbMapper.map(barcode, upcResult)
            ProductLookupResult.FoundBasicInfo(product)
        } else {
            ProductLookupResult.NotFound
        }
    }

    private suspend fun fetchUpcItemDbImage(barcode: String): String? = try {
        RetrofitClient.upcItemDbApi.lookup(barcode).items
            ?.firstOrNull()?.images?.firstOrNull()
    } catch (e: Exception) {
        null
    }

    suspend fun searchProducts(query: String): List<Product> = try {
        RetrofitClient.openFoodFactsApi.searchProducts(query).products.map { ProductMapper.map(it) }
    } catch (e: Exception) {
        emptyList()
    }

    /**
     * Sends a scanned product (barcode + whatever the person typed / read
     * from the label OCR) back to Open Food Facts, so the next person who
     * scans the same barcode gets a full match instead of a fallback.
     * Requires a free Open Food Facts account — create one at
     * https://world.openfoodfacts.org/cgi/user.pl — no cost, ever.
     */
    suspend fun submitProductToOpenFoodFacts(
        barcode: String,
        productName: String,
        brand: String?,
        ingredientsText: String?,
        offUsername: String,
        offPassword: String
    ): ContributionResult = try {
        val response = RetrofitClient.openFoodFactsApi.submitProduct(
            code = barcode,
            userId = offUsername,
            password = offPassword,
            productName = productName.takeIf { it.isNotBlank() },
            brands = brand?.takeIf { it.isNotBlank() },
            ingredientsText = ingredientsText?.takeIf { it.isNotBlank() }
        )
        if (response.status == 1) {
            ContributionResult.Success
        } else {
            ContributionResult.Failure(response.statusVerbose ?: "unknown_error")
        }
    } catch (e: Exception) {
        ContributionResult.Failure(e.message ?: "unknown_error")
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

    suspend fun deleteHistoryItem(id: Long) = db.scanHistoryDao().deleteById(id)

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
