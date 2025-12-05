package com.jdeguzman.checkcheqapp.data.local.dao

import androidx.room.*
import com.jdeguzman.checkcheqapp.data.local.entity.BasketItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for accessing and mutating [BasketItemEntity] rows.
 *
 * This is used for quick, local basket tracking that is separate
 * from the map price posts and Firestore-backed data.
 */
@Dao
interface BasketDao {

    /**
     * Observe the full list of basket items as a [Flow].
     *
     * The flow emits a new list whenever the table changes.
     */
    @Query("SELECT * FROM BasketItem")
    fun observeAll(): Flow<List<BasketItemEntity>>

    /**
     * Insert or replace a basket item.
     *
     * If an item with the same primary key already exists, it is replaced.
     *
     * @param b the entity to insert or update.
     * @return the row ID of the inserted/updated entity.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(b: BasketItemEntity): Long

    /**
     * Delete a single basket item row.
     *
     * @param item the entity to remove.
     */
    @Delete
    suspend fun delete(item: BasketItemEntity)

    /**
     * Remove all rows from the `BasketItem` table.
     *
     * Use with care – this clears the entire basket.
     */
    @Query("DELETE FROM BasketItem")
    suspend fun clear()
}
