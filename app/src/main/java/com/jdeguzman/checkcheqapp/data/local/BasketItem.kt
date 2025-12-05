package com.jdeguzman.checkcheqapp.data.local

import com.jdeguzman.checkcheqapp.data.local.entity.BasketItemEntity

/**
 * Domain/UI model for basket items.
 *
 * This is separate from the Room entity so that the UI layer
 * is not tightly coupled to Room-specific types.
 */
data class BasketItem(
    val id: Long = 0,
    val name: String,
    val qty: Int,
    val price: Double
)

/**
 * Map a [BasketItemEntity] from Room into the [BasketItem] domain model.
 *
 * @return a domain model copy of this entity.
 */
fun BasketItemEntity.toDomain(): BasketItem =
    BasketItem(
        id = id,
        name = name,
        qty = qty,
        price = price
    )

/**
 * Map a [BasketItem] domain model into a [BasketItemEntity] for Room.
 *
 * @return a Room entity copy of this domain model.
 */
fun BasketItem.toEntity(): BasketItemEntity =
    BasketItemEntity(
        id = id,
        name = name,
        qty = qty,
        price = price
    )
