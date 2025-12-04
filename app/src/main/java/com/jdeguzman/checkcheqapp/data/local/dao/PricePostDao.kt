package com.jdeguzman.checkcheqapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jdeguzman.checkcheqapp.data.local.entity.PricePostEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing locally cached price posts.
 *
 * Backed by the `price_posts` Room table.
 * Used by the app to store and observe all price pins shown on the map/feed.
 */
@Dao
interface PricePostDao {

    /**
     * Observe all price posts ordered from newest to oldest.
     *
     * @return a [Flow] that emits the full list whenever the table changes.
     */
    @Query("SELECT * FROM price_posts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PricePostEntity>>

    /**
     * Fetch a single price post by its primary key.
     *
     * @param id the `id` column of the row.
     * @return the matching [PricePostEntity], or `null` if no row exists.
     */
    @Query("SELECT * FROM price_posts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PricePostEntity?

    /**
     * Insert or replace a price post.
     *
     * If a row with the same primary key already exists, it will be replaced.
     *
     * @param post the entity to insert.
     * @return the row ID of the inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(post: PricePostEntity): Long

    /**
     * Update an existing price post.
     *
     * The row is matched on primary key. If no row exists, nothing happens.
     *
     * @param post the entity with updated fields.
     */
    @Update
    suspend fun update(post: PricePostEntity)

    /**
     * Delete a single price post by its ID.
     *
     * @param id the `id` of the row to remove.
     */
    @Query("DELETE FROM price_posts WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Delete all price posts from the table.
     *
     * Use with care — this clears the entire local cache of posts.
     */
    @Query("DELETE FROM price_posts")
    suspend fun clear()
}
