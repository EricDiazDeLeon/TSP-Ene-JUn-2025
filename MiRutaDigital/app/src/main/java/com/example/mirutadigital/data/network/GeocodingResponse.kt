package com.example.mirutadigital.data.network

import com.squareup.moshi.Json

data class GeocodingResponse(
    @field:Json(name = "results") val results: List<GeocodingResult>
)

data class GeocodingResult(
    @field:Json(name = "formatted_address") val formattedAddress: String?,
    @field:Json(name = "geometry") val geometry: Geometry
)

data class Geometry(
    @field:Json(name = "location") val location: Location
)

data class Location(
    @field:Json(name = "lat") val lat: Double,
    @field:Json(name = "lng") val lng: Double
)
