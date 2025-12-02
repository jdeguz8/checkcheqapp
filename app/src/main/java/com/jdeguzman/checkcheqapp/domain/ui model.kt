package com.jdeguzman.checkcheqapp.domain
/**
 * Domain model representing a single price post.
 *
 * This is the main data type used by the UI and ViewModels.
 * A post is tied to a real-world location and may include:
 * - store / restaurant name
 * - item or dish name
 * - price
 * - latitude / longitude
 * - optional photo URL/URI
 * - creation timestamp
 * - optional category (Grocery, Restaurant, etc.)
 * - optional "postedBy" display name or email
 */
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
