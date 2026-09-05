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

    @Query("SELECT * FROM scan_history ORDER BY timestampMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ScanHistoryEntity>>

    @Query("SELECT AVG(score) FROM scan_history WHERE timestampMillis >= :sinceMillis")
    suspend fun averageScoreSince(sinceMillis: Long): Double?

    @Query("SELECT COUNT(*) FROM scan_history")
    suspend fun totalScanned(): Int

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

    @Query("DELETE FROM basket_items")
    suspend fun clear()
}
