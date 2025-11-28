package com.jdeguzman.checkcheqapp.data.repository

import com.jdeguzman.checkcheqapp.data.local.dao.PricePostDao
import com.jdeguzman.checkcheqapp.data.local.entity.PricePostEntity
import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class RoomPricePostRepository @Inject constructor(
    private val dao: PricePostDao
) : PricePostRepository {

    override fun observePosts(): Flow<List<PricePost>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(post: PricePost) {
        dao.upsert(post.toEntity())
    }

    override suspend fun clear() {
        dao.clear()
    }
}

// --- mappers ---

private fun PricePostEntity.toDomain(): PricePost =
    PricePost(
        id = id,
        storeName = storeName,
        itemName = itemName,
        price = price,
        lat = lat,
        lng = lng,
        photoUri = photoUri,
        createdAt = createdAt
    )

private fun PricePost.toEntity(): PricePostEntity =
    PricePostEntity(
        id = id,
        storeName = storeName,
        itemName = itemName,
        price = price,
        lat = lat,
        lng = lng,
        photoUri = photoUri,
        createdAt = createdAt
    )
