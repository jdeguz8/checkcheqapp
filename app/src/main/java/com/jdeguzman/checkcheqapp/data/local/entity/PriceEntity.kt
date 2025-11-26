package com.jdeguzman.checkcheqapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prices")
data class PriceEntity(
    @PrimaryKey val sku: String,
    val store: String,
    val currentPrice: Double,
    val lastUpdated: Long,
    val itemId: Long
)
