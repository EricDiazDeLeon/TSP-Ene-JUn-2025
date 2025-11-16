package com.example.mirutadigital.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleApiService {

    @GET("maps/api/geocode/json")
    suspend fun getCoordinates(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GeocodingResponse
}
