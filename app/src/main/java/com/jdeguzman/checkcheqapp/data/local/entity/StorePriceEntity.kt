package com.jdeguzman.checkcheqapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_prices")
data class StorePriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lat: Double,
    val lng: Double,
    val storeName: String,
    val itemName: String,
    val price: Double,
    val createdAt: Long = System.currentTimeMillis()
)
