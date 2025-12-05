package com.jdeguzman.checkcheqapp.data.repository

import android.util.Log
import com.jdeguzman.checkcheqapp.data.remote.yelp.YelpApiService
import com.jdeguzman.checkcheqapp.data.remote.yelp.YelpBusiness
import com.jdeguzman.checkcheqapp.domain.PricePost
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that encapsulates Yelp search logic for a given [PricePost].
 */
@Singleton
class YelpRepository @Inject constructor(
    private val api: YelpApiService
) {

    /**
     * Try to find the best matching [YelpBusiness] for a given [post].
     *
     * Heuristics:
     * - Search with store name as the term.
     * - Optionally map category to Yelp categories.
     * - Prefer businesses whose name contains the store name.
     * - Fallback to the closest business by distance.
     *
     * @return the best matching business or `null` if none match or an error occurs.
     */
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
                limit = 10
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
