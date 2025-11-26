package com.jdeguzman.checkcheqapp.data.remote

import com.jdeguzman.checkcheqapp.data.remote.model.PriceDto
import retrofit2.http.GET

interface ApiService {
    // TODO: Replace with your real endpoint
    @GET("prices.json")
    suspend fun getPrices(): List<PriceDto>
}
