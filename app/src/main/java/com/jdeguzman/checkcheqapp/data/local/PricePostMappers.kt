package com.jdeguzman.checkcheqapp.data.local

import com.jdeguzman.checkcheqapp.data.local.entity.PricePostEntity
import com.jdeguzman.checkcheqapp.domain.PricePost

/**
 * Map a [PricePostEntity] from Room to the domain-level [PricePost] model.
 *
 * Use this when reading from the database and exposing price posts to the UI
 * or other layers that should not depend directly on Room entities.
 */
fun PricePostEntity.toDomain(): PricePost =
    PricePost(
        id = id,
        storeName = storeName,
        itemName = itemName,
        price = price,
        lat = lat,
        lng = lng,
        photoUri = photoUri,
        createdAt = createdAt,
        category = category,
        postedBy = postedBy,
        ownerUid = ownerUid
    )

/**
 * Map a domain-level [PricePost] model to the Room [PricePostEntity].
 *
 * Use this when persisting posts back into the database from repositories
 * or view models.
 */
fun PricePost.toEntity(): PricePostEntity =
    PricePostEntity(
        id = id,
        storeName = storeName,
        itemName = itemName,
        price = price,
        lat = lat,
        lng = lng,
        photoUri = photoUri,
        createdAt = createdAt,
        category = category,
        postedBy = postedBy,
        ownerUid = ownerUid
    )
