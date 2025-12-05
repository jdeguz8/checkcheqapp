package com.jdeguzman.checkcheqapp.data.repository

import com.jdeguzman.checkcheqapp.data.local.dao.PricePostDao
import com.jdeguzman.checkcheqapp.data.local.toDomain
import com.jdeguzman.checkcheqapp.data.local.toEntity
import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple repository for observing and saving price posts in Room.
 *
 * This is a thin wrapper over [PricePostDao] used for local caching.
 */
@Singleton
class PriceRepository @Inject constructor(
    private val pricePostDao: PricePostDao
) {

    /**
     * Observe all price posts from Room as domain models.
     */
    fun observePosts(): Flow<List<PricePost>> =
        pricePostDao.observeAll().map { list -> list.map { it.toDomain() } }

    /**
     * Insert or update a post in Room.
     *
     * @param post the domain model to persist.
     */
    suspend fun addPost(post: PricePost) {
        pricePostDao.upsert(post.toEntity())
    }

    /**
     * Clear all cached posts from Room.
     */
    suspend fun clearAll() {
        pricePostDao.clear()
    }
}
