// AppDatabase.kt
package com.jdeguzman.checkcheqapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jdeguzman.checkcheqapp.data.local.dao.BasketDao
import com.jdeguzman.checkcheqapp.data.local.dao.ItemDao
import com.jdeguzman.checkcheqapp.data.local.dao.PricePostDao
import com.jdeguzman.checkcheqapp.data.local.entity.BasketItemEntity
import com.jdeguzman.checkcheqapp.data.local.entity.ItemEntity
import com.jdeguzman.checkcheqapp.data.local.entity.PricePostEntity

@Database(
    entities = [
        ItemEntity::class,
        BasketItemEntity::class,
        PricePostEntity::class
    ],
    version = 14,          // bump this so Room recreates the DB
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun basketDao(): BasketDao
    abstract fun pricePostDao(): PricePostDao
}
