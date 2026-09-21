package com.healthyscan.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Insert
    suspend fun insert(entity: ScanHistoryEntity)

    @Query("SELECT * FROM scan_history ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history ORDER BY timestampMillis DESC")
    suspend fun getAllOnce(): List<ScanHistoryEntity>

    @Query("SELECT * FROM scan_history ORDER BY timestampMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ScanHistoryEntity>>

    /** Most recent cached entry for this barcode, if any — used to reopen a
     *  product from History/Favorites/Home without re-scanning it. */
    @Query("SELECT * FROM scan_history WHERE barcode = :barcode ORDER BY timestampMillis DESC LIMIT 1")
    suspend fun getMostRecentByBarcode(barcode: String): ScanHistoryEntity?

    @Query("SELECT AVG(score) FROM scan_history WHERE timestampMillis >= :sinceMillis")
    suspend fun averageScoreSince(sinceMillis: Long): Double?

    /** All-time average — what "My stats" in Profile actually shows, so it
     *  never silently reads as 0 just because the scans happened more than
     *  a week ago. */
    @Query("SELECT AVG(score) FROM scan_history")
    suspend fun averageScoreAllTime(): Double?

    @Query("SELECT COUNT(*) FROM scan_history")
    suspend fun totalScanned(): Int

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM scan_history")
    suspend fun clear()
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Delete
    suspend fun delete(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE barcode = :barcode")
    suspend fun deleteByBarcode(barcode: String)

    @Query("SELECT * FROM favorites ORDER BY addedAtMillis DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites ORDER BY addedAtMillis DESC")
    suspend fun getAllOnce(): List<FavoriteEntity>

    @Query("SELECT * FROM favorites WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): FavoriteEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE barcode = :barcode)")
    fun isFavorite(barcode: String): Flow<Boolean>
}

@Dao
interface BasketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BasketItemEntity)

    @Query("DELETE FROM basket_items WHERE barcode = :barcode")
    suspend fun deleteByBarcode(barcode: String)

    @Query("SELECT * FROM basket_items ORDER BY addedAtMillis DESC")
    fun observeAll(): Flow<List<BasketItemEntity>>

    @Query("SELECT * FROM basket_items ORDER BY addedAtMillis DESC")
    suspend fun getAllOnce(): List<BasketItemEntity>

    @Query("DELETE FROM basket_items")
    suspend fun clear()
}
