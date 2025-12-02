package com.jdeguzman.checkcheqapp.data.repository

import com.jdeguzman.checkcheqapp.data.local.dao.PricePostDao
import com.jdeguzman.checkcheqapp.data.local.toDomain
import com.jdeguzman.checkcheqapp.data.local.toEntity
import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room-backed implementation of [PricePostRepository].
 *
 * Persists posts in the local price_posts table and maps
 * between Room entities and the domain [PricePost] model.
 */
class RoomPricePostRepository @Inject constructor(
    private val dao: PricePostDao
) : PricePostRepository {

    /**
     * Observe posts from Room, mapping each entity to a domain model.
     */
    override fun observePosts(): Flow<List<PricePost>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    /**
     * Upsert a post into Room and return the row ID.
     */
    override suspend fun add(post: PricePost): Long {
        return dao.upsert(post.toEntity())
    }

    /**
     * Clear all posts from Room.
     */
    override suspend fun clear() {
        dao.clear()
    }
}
