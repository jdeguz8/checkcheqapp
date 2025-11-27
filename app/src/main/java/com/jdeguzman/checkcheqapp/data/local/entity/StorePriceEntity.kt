package com.jdeguzman.checkcheqapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_prices")
data class StorePriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeName: String,
    val lat: Double,
    val lng: Double,
    val itemName: String,
    val price: Double,
    val currency: String = "CAD",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)


