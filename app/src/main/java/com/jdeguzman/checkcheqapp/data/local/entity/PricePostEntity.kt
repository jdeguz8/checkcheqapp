package com.jdeguzman.checkcheqapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a price post row in the local database.
 *
 * Mirrors the Firestore document fields so we can cache posts offline.
 */
@Entity(tableName = "price_posts")
data class PricePostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeName: String,
    val itemName: String,
    val price: Double,
    val lat: Double,
    val lng: Double,
    val photoUri: String?,
    val createdAt: Long,
    val category: String? = null,
    val postedBy: String? = null
)
