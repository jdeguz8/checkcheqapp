package com.jdeguzman.checkcheqapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a saved item in the master item list.
 *
 * Uses a unique index on name so we can safely upsert by name.
 */
@Entity(
    tableName = "items",
    indices = [Index(value = ["name"], unique = true)]
)

/**
 main entity item for ItemEntity
 */
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double? = null,
    val brand: String? = null
)