// data/repository/YelpRepository.kt
package com.jdeguzman.checkcheqapp.data.repository

import android.util.Log
import com.jdeguzman.checkcheqapp.data.remote.yelp.YelpApiService
import com.jdeguzman.checkcheqapp.data.remote.yelp.YelpBusiness
import com.jdeguzman.checkcheqapp.domain.PricePost
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YelpRepository @Inject constructor(
    private val api: YelpApiService
) {

    suspend fun findBestMatchForPost(post: PricePost): YelpBusiness? {
        return try {
            val term = post.storeName // keep it simple; item name can confuse things

            val categories = when (post.category?.lowercase()) {
                "restaurant", "fast food" -> "restaurants"
                "cafe" -> "cafes,coffee"
                "bakery" -> "bakeries"
                else -> null
            }

            Log.d(
                "YelpRepository",
                "Searching Yelp: term='$term', lat=${post.lat}, lng=${post.lng}, radius=2000, categories=$categories"
            )

            val response = api.searchBusinesses(
                term = term,
                latitude = post.lat,
                longitude = post.lng,
                radius = 2000,
                categories = categories,
                limit = 10        // give us a few more to choose from
            )

            if (response.businesses.isEmpty()) {
                Log.d("YelpRepository", "No businesses returned for postId=${post.id}")
                return null
            }

            val normalizedStore = post.storeName.trim().lowercase()

            // 1) Prefer name that contains the store name (case-insensitive)
            val nameMatch = response.businesses
                .sortedBy { it.distance ?: Double.MAX_VALUE }
                .firstOrNull { biz ->
                    biz.name.trim().lowercase().contains(normalizedStore)
                }

            val best = nameMatch
                ?: response.businesses.minByOrNull { it.distance ?: Double.MAX_VALUE }

            best?.let {
                Log.d("YelpRepository", "Matched Yelp business '${it.name}' for postId=${post.id}")
            }

            best
        } catch (e: Exception) {
            Log.e("YelpRepository", "Yelp API error for postId=${post.id}", e)
            null
        }
    }
}
