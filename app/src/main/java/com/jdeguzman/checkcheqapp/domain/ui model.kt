package com.jdeguzman.checkcheqapp.domain

data class PricePost(
    val id: Long = 0L,
    val storeName: String,
    val itemName: String,
    val price: Double,
    val lat: Double,
    val lng: Double,
    val photoUri: String?,       // local URI or remote URL
    val createdAt: Long,
    val category: String? = null, // Grocery / Restaurant / etc.
    val postedBy: String? = null  // NEW: display name or email
)
