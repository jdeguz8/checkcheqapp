package com.jdeguzman.checkcheqapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single row in the `BasketItem` table.
 *
 * Used for quick, local basket lists separate from the map price posts.
 */
@Entity(tableName = "BasketItem")
data class BasketItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val qty: Int,
    val price: Double
)
