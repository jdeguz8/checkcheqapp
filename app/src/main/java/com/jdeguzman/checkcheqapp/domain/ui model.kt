package com.jdeguzman.checkcheqapp.domain

data class PricePost(
    val id: Long = 0L,                      // Room will auto-generate
    val storeName: String,
    val itemName: String,
    val price: Double,
    val lat: Double,
    val lng: Double,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
