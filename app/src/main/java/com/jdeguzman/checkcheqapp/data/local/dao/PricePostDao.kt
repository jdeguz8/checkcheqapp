package com.jdeguzman.checkcheqapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jdeguzman.checkcheqapp.data.local.entity.PricePostEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for locally cached price posts.
 *
 * Right now this is mostly used as an optional cache alongside Firestore.
 */
@Dao
interface PricePostDao {

    /**
     * Observe all posts ordered by creation time (newest first).
     */
    @Query("SELECT * FROM price_posts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PricePostEntity>>

    /**
     * Insert or replace a single post.
     *
     * @return the row ID of the inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(post: PricePostEntity): Long

    /**
     * Delete all posts from the local price_posts table.
     */
    @Query("DELETE FROM price_posts")
    suspend fun clear()
}
