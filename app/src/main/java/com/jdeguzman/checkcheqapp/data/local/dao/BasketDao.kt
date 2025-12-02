package com.jdeguzman.checkcheqapp.data.local.dao

import androidx.room.*
import com.jdeguzman.checkcheqapp.data.local.BasketItem
import com.jdeguzman.checkcheqapp.data.local.entity.BasketItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the BasketItem table.
 *
 * Used for quick, local basket tracking separate from the map feed.
 */
@Dao
interface BasketDao {

    /**
     * Observe the full list of basket items as a Flow.
     */
    @Query("SELECT * FROM BasketItem")
    fun observeAll(): Flow<List<BasketItemEntity>>

    /**
     * Insert or replace a basket item.
     *
     * @return the row ID of the upserted item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(b: BasketItemEntity): Long

    /**
     * Delete a single basket item.
     */
    @Delete
    suspend fun delete(item: BasketItemEntity)

    /**
     * Remove all rows from the BasketItem table.
     */
    @Query("DELETE FROM BasketItem")
    suspend fun clear()
}