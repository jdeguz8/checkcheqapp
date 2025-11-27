package com.jdeguzman.checkcheqapp

data class RemoteStorePrice(
    val storeName: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val itemName: String = "",
    val price: Double = 0.0,
    val currency: String = "CAD",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
