package com.jdeguzman.checkcheqapp.data.remote.yelp

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Lightweight representation of a Yelp business returned from the
 * `/businesses/search` endpoint.
 */
data class YelpBusiness(
    val id: String,
    val name: String,
    val rating: Double? = null,
    val review_count: Int? = null,
    val price: String? = null,
    val url: String? = null,
    val distance: Double? = null
)

/**
 * Response wrapper for Yelp business search.
 */
data class YelpSearchResponse(
    val businesses: List<YelpBusiness> = emptyList()
)

/**
 * Retrofit service for calling the Yelp Fusion API.
 */
interface YelpApiService {

    /**
     * Search Yelp businesses around a coordinate.
     *
     * @param term free-text search term (e.g., "Starbucks", "pizza").
     * @param latitude latitude of the search origin.
     * @param longitude longitude of the search origin.
     * @param radius optional radius in meters.
     * @param categories comma-separated category filters (Yelp category slugs).
     * @param limit maximum number of results to return.
     * @param sortBy sort order, e.g. "distance".
     */
    @GET("businesses/search")
    suspend fun searchBusinesses(
        @Query("term") term: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int? = null,
        @Query("categories") categories: String? = null,
        @Query("limit") limit: Int = 3,
        @Query("sort_by") sortBy: String = "distance"
    ): YelpSearchResponse
}
