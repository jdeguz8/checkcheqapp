package com.jdeguzman.checkcheqapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jdeguzman.checkcheqapp.data.local.entity.StorePriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StorePriceDao {

    @Query("SELECT * FROM store_prices ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<StorePriceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StorePriceEntity): Long

    @Query("DELETE FROM store_prices")
    suspend fun clear()
}
