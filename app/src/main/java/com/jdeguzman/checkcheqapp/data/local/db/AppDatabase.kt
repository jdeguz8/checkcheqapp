package com.jdeguzman.checkcheqapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jdeguzman.checkcheqapp.data.local.dao.BasketDao
import com.jdeguzman.checkcheqapp.data.local.dao.PriceDao
import com.jdeguzman.checkcheqapp.data.local.dao.StorePriceDao
import com.jdeguzman.checkcheqapp.data.local.entity.BasketItemEntity
import com.jdeguzman.checkcheqapp.data.local.entity.ItemEntity
import com.jdeguzman.checkcheqapp.data.local.entity.PriceEntity
import com.jdeguzman.checkcheqapp.data.local.entity.StorePriceEntity
import com.jdeguzman.checkcheqapp.data.local.dao.ItemDao
@Database(
    entities = [
        ItemEntity::class,
        BasketItemEntity::class,
        PriceEntity::class,
        StorePriceEntity::class
    ],
    version = 3,           // ⬅ bumped from 2 → 3
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun basketDao(): BasketDao
    abstract fun priceDao(): PriceDao
    abstract fun itemDao(): ItemDao
    abstract fun storePriceDao(): StorePriceDao
}
