package com.jdeguzman.checkcheqapp.data.repository

import com.jdeguzman.checkcheqapp.data.local.dao.PricePostDao
import com.jdeguzman.checkcheqapp.data.local.toDomain
import com.jdeguzman.checkcheqapp.data.local.toEntity
import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomPricePostRepository @Inject constructor(
    private val dao: PricePostDao
) : PricePostRepository {

    override fun observePosts(): Flow<List<PricePost>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(post: PricePost): Long {
        return dao.upsert(post.toEntity())   // rowId from Room
    }

    override suspend fun update(post: PricePost) {
        dao.update(post.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun clear() {
        dao.clear()
    }
}
