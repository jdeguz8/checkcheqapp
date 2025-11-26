package com.jdeguzman.checkcheqapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Basic item row saved in Room.
 * - Unique index on name so REPLACE upserts by name work.
 */
@Entity(
    tableName = "items",
    indices = [Index(value = ["name"], unique = true)]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double? = null,
    val brand: String? = null
)