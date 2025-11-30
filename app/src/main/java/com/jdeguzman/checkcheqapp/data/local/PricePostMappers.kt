package com.jdeguzman.checkcheqapp.data.local

import com.jdeguzman.checkcheqapp.data.local.entity.PricePostEntity
import com.jdeguzman.checkcheqapp.domain.PricePost

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
        postedBy = postedBy
    )

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
        postedBy = postedBy
    )
