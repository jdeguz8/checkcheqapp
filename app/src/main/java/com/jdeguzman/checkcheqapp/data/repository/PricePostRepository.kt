// PricePostRepository.kt
package com.jdeguzman.checkcheqapp.data.repository

import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for working with price posts in local storage.
 *
 * This interface is implemented by Room-backed repositories and
 * could later be extended to add remote syncing logic.
 */
interface PricePostRepository {

    /**
     * Observe the list of all price posts as a Flow.
     */
    fun observePosts(): Flow<List<PricePost>>

    /**
     * Insert or update a single post.
     *
     * @return the ID of the stored post.
     */
    suspend fun add(post: PricePost): Long

    /**
     * Remove all locally stored posts.
     */

    suspend fun update(post: PricePost)
    suspend fun deleteById(id: Long)

    /**
     * Remove all locally stored posts.
     */
    suspend fun clear()
}
