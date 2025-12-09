package com.jdeguzman.checkcheqapp.data.repository

import com.jdeguzman.checkcheqapp.data.local.dao.PricePostDao
import com.jdeguzman.checkcheqapp.data.local.toDomain
import com.jdeguzman.checkcheqapp.data.local.toEntity
import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [PricePostRepository] implementation backed by Room [PricePostDao].
 *
 * This is used as the primary local data source for price posts.
 */
class RoomPricePostRepository @Inject constructor(
    private val dao: PricePostDao
) : PricePostRepository {

    /**
     * Observe all posts from Room as domain models.
     */
    override fun observePosts(): Flow<List<PricePost>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    /**
     * Insert or update a post in Room.
     *
     * @param post the post to store.
     * @return the inserted/updated row ID.
     */
    override suspend fun add(post: PricePost): Long {
        return dao.upsert(post.toEntity())
    }

    /**
     * Update an existing Room post.
     *
     * @param post the updated post.
     */
    override suspend fun update(post: PricePost) {
        dao.update(post.toEntity())
    }

    /**
     * Delete a post by its local ID.
     *
     * @param id the row ID of the post to delete.
     */
    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    /**
     * Clear all posts from the table.
     */
    override suspend fun clear() {
        dao.clear()
    }

    /**
     * Clear all posts from the table after 7 days
     */
    override suspend fun deleteOlderThan(cutoffMillis: Long) {
        dao.deleteOlderThan(cutoffMillis)
    }
}
