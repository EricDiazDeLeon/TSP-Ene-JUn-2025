package com.example.mirutadigital.data.model.ui


data class RouteDetailInfo(
    val id: String,
    val name: String,
    val departureInterval: Int,
    val windshieldLabel: String,
    val colors: String,
    val price: Double,
    val outboundJourney: JourneyDetailInfo,
    val inboundJourney: JourneyDetailInfo
)

data class JourneyDetailInfo(
    val startStopName: String,
    val endStopName: String,
    val firstDeparture: String,
    val lastDeparture: String
)
