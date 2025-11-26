package com.jdeguzman.checkcheqapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jdeguzman.checkcheqapp.data.local.dao.ItemDao
import com.jdeguzman.checkcheqapp.data.local.dao.BasketDao
import com.jdeguzman.checkcheqapp.data.local.dao.PriceDao
import com.jdeguzman.checkcheqapp.data.local.entity.BasketItemEntity
import com.jdeguzman.checkcheqapp.data.local.entity.PriceEntity
import com.jdeguzman.checkcheqapp.data.local.entity.ItemEntity


@Database(
    entities = [
        ItemEntity::class,
        BasketItemEntity::class,
        PriceEntity::class,
        // add any additional entities here
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun basketDao(): BasketDao
    abstract fun priceDao(): PriceDao
    abstract fun itemDao(): ItemDao

}
