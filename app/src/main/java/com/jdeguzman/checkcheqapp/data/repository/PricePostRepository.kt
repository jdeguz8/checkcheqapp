package com.jdeguzman.checkcheqapp.data.repository

import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for working with price posts in local storage.
 *
 * Implementations may be backed by Room, Firestore, or composite sources.
 */
interface PricePostRepository {

    /**
     * Observe the full list of price posts as a [Flow].
     *
     * @return stream of [PricePost] lists ordered as defined by the implementation.
     */
    fun observePosts(): Flow<List<PricePost>>

    /**
     * Insert or update a single post.
     *
     * @param post the post to persist.
     * @return the local ID of the stored post.
     */
    suspend fun add(post: PricePost): Long

    /**
     * Update an existing post.
     *
     * The behavior depends on the underlying storage implementation.
     *
     * @param post the post with updated fields.
     */
    suspend fun update(post: PricePost)

    /**
     * Delete a single post by its ID.
     *
     * @param id the local ID of the post to remove.
     */
    suspend fun deleteById(id: Long)

    /**
     * Remove all locally stored posts.
     */
    suspend fun clear()

    /**
     * Delete all posts older than [cutoffMillis].
     */
    suspend fun deleteOlderThan(cutoffMillis: Long)
}
