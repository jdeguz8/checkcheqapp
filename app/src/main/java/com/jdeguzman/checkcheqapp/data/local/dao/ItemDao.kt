package com.jdeguzman.checkcheqapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.jdeguzman.checkcheqapp.data.local.entity.ItemEntity


/**
 * DAO for the items table.
 *
 * Manages the master list of items (separate from posts on the map).
 */
@Dao
interface ItemDao {

    /**
     * Observe all items ordered by name.
     */
    @Query("SELECT * FROM items ORDER BY name")
    fun observeAll(): Flow<List<ItemEntity>>

    /**
     * Look up a single item by its primary key.
     */
    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ItemEntity?

    /**
     * Look up a single item by its name (case-insensitive).
     */
    @Query("SELECT * FROM items WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getByName(name: String): ItemEntity?

    /**
     * Search items by partial name match.
     */
    @Query("SELECT * FROM items WHERE name LIKE '%' || :q || '%' ORDER BY name")
    fun search(q: String): Flow<List<ItemEntity>>

    /**
     * Insert or replace one or more items.
     *
     * @return list of new row IDs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg items: ItemEntity): List<Long>

    /**
     * Update an existing item row.
     */
    @Update
    suspend fun update(item: ItemEntity)

    /**
     * Delete an item row.
     */
    @Delete
    suspend fun delete(item: ItemEntity)
}