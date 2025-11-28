package com.jdeguzman.checkcheqapp.data.repository

import com.jdeguzman.checkcheqapp.data.local.dao.PricePostDao
import com.jdeguzman.checkcheqapp.data.local.toDomain
import com.jdeguzman.checkcheqapp.data.local.toEntity
import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRepository @Inject constructor(
    private val pricePostDao: PricePostDao
) {
    fun observePosts(): Flow<List<PricePost>> =
        pricePostDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun addPost(post: PricePost) {
        pricePostDao.upsert(post.toEntity())
    }

    suspend fun clearAll() {
        pricePostDao.clear()
    }
}
