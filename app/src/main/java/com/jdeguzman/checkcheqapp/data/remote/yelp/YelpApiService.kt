// data/remote/yelp/YelpApiService.kt
package com.jdeguzman.checkcheqapp.data.remote.yelp

import retrofit2.http.GET
import retrofit2.http.Query

data class YelpBusiness(
    val id: String,
    val name: String,
    val rating: Double? = null,
    val review_count: Int? = null,
    val price: String? = null,
    val url: String? = null,
    val distance: Double? = null      // <-- meters from the search point
)

data class YelpSearchResponse(
    val businesses: List<YelpBusiness> = emptyList()
)

interface YelpApiService {

    @GET("businesses/search")
    suspend fun searchBusinesses(
        @Query("term") term: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int? = null,
        @Query("categories") categories: String? = null,
        @Query("limit") limit: Int = 3,
        @Query("sort_by") sortBy: String = "distance"   // <-- important
    ): YelpSearchResponse
}
