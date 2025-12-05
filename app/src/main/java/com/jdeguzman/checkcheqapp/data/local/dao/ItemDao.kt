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
 * Room DAO for the `items` table.
 *
 * This manages the master list of items, separate from map posts.
 */
@Dao
interface ItemDao {

    /**
     * Observe all items ordered alphabetically by name.
     *
     * @return [Flow] emitting the full list whenever the table changes.
     */
    @Query("SELECT * FROM items ORDER BY name")
    fun observeAll(): Flow<List<ItemEntity>>

    /**
     * Look up a single item by its primary key.
     *
     * @param id the item ID.
     * @return the matching [ItemEntity] or `null` if not found.
     */
    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ItemEntity?

    /**
     * Look up a single item by its name (case-insensitive).
     *
     * @param name the exact item name to search for.
     * @return the matching [ItemEntity] or `null` if not found.
     */
    @Query("SELECT * FROM items WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getByName(name: String): ItemEntity?

    /**
     * Search items by a partial name match.
     *
     * @param q text fragment to search within the item name.
     * @return [Flow] that emits search results ordered by name.
     */
    @Query("SELECT * FROM items WHERE name LIKE '%' || :q || '%' ORDER BY name")
    fun search(q: String): Flow<List<ItemEntity>>

    /**
     * Insert or replace one or more items.
     *
     * If an item with the same primary key exists, it is replaced.
     *
     * @param items the items to upsert.
     * @return list of new row IDs (one per item).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg items: ItemEntity): List<Long>

    /**
     * Update an existing item row.
     *
     * The row is matched by primary key.
     *
     * @param item the entity with updated fields.
     */
    @Update
    suspend fun update(item: ItemEntity)

    /**
     * Delete an item row.
     *
     * @param item the entity to remove.
     */
    @Delete
    suspend fun delete(item: ItemEntity)
}
