package com.jdeguzman.checkcheqapp.data.local

import com.jdeguzman.checkcheqapp.data.local.entity.BasketItemEntity

/**
 * Domain/UI model for basket items.
 * Separate from the Room entity so UI isn't tied to Room.
 */
data class BasketItem(
    val id: Long = 0,
    val name: String,
    val qty: Int,
    val price: Double
)

// --- mapping helpers between Entity and domain model ---

/**
 * Entity to domain model helper
 * */
fun BasketItemEntity.toDomain(): BasketItem =
    BasketItem(
        id = id,
        name = name,
        qty = qty,
        price = price
    )

/**
 * Entity to domain model helper
 * */
fun BasketItem.toEntity(): BasketItemEntity =
    BasketItemEntity(
        id = id,
        name = name,
        qty = qty,
        price = price
    )