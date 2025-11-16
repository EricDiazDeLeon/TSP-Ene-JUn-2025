package com.example.mirutadigital.testsUi.dataSource

import com.example.mirutadigital.testsUi.model.route.Journey
import com.example.mirutadigital.testsUi.sampleRoutes

data class JourneyDetailInfo(
    val startStopName: String, // Nombre de la primera parada
    val endStopName: String,   // Nombre de la última parada
    val firstDeparture: String,
    val lastDeparture: String
)

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

private fun Journey.toJourneyDetailInfo(): JourneyDetailInfo {
    val start = this.stops.firstOrNull()?.name ?: "N/A"
    val end = this.stops.lastOrNull()?.name ?: "N/A"

    return JourneyDetailInfo(
        startStopName = start,
        endStopName = end,
        firstDeparture = this.firstDeparture,
        lastDeparture = this.lastDeparture
    )
}

fun getRouteDetailInfoById(routeId: String): RouteDetailInfo? {
    val route = sampleRoutes.find { it.id == routeId } ?: return null

    return RouteDetailInfo(
        id = route.id,
        name = route.name,
        departureInterval = route.departureInterval,
        windshieldLabel = route.detail?.windshieldLabel ?: "N/A",
        colors = route.detail?.colors ?: "",
        price = route.detail?.price ?: 0.0,
        outboundJourney = route.outboundJourney.toJourneyDetailInfo(),
        inboundJourney = route.inboundJourney.toJourneyDetailInfo()
    )
}


