package com.healthyscan.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single scan event. We keep a lightweight snapshot (name/brand/score) so
 * History and stats screens don't need a network round-trip, plus the raw
 * [productJson] so the full Product Result screen can be reopened offline.
 */
@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String,
    val name: String,
    val brand: String?,
    val imageUrl: String?,
    val score: Int,
    val timestampMillis: Long,
    val productJson: String
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val barcode: String,
    val name: String,
    val brand: String?,
    val imageUrl: String?,
    val score: Int,
    val addedAtMillis: Long,
    val productJson: String
)

@Entity(tableName = "basket_items")
data class BasketItemEntity(
    @PrimaryKey val barcode: String,
    val name: String,
    val imageUrl: String?,
    val score: Int,
    val addedAtMillis: Long
)
