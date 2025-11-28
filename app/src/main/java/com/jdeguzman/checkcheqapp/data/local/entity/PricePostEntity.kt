package com.jdeguzman.checkcheqapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room representation of a price post.
 */
@Entity(tableName = "price_posts")
data class PricePostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeName: String,
    val itemName: String,
    val price: Double,
    val lat: Double,
    val lng: Double,
    val photoUri: String?,        // same name as domain
    val createdAt: Long           // millis since epoch
)
