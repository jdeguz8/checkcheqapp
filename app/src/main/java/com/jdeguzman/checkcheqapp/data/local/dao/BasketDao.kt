package com.jdeguzman.checkcheqapp.data.local.dao

import androidx.room.*
import com.jdeguzman.checkcheqapp.data.local.BasketItem
import com.jdeguzman.checkcheqapp.data.local.entity.BasketItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BasketDao {
    @Query("SELECT * FROM BasketItem")
    fun observeAll(): Flow<List<BasketItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(b: BasketItemEntity): Long

    @Delete
    suspend fun delete(item: BasketItemEntity)

    @Query("DELETE FROM BasketItem")
    suspend fun clear()
}
