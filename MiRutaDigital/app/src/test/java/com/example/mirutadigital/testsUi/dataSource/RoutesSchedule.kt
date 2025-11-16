package com.example.mirutadigital.testsUi.dataSource

import com.example.mirutadigital.testsUi.sampleRoutes

data class Schedule(
    val nameOrigin: String,
    val nameDestination: String,
    val schedule: Pair<String, String>, // hh:mm - hh:mm
)

// modelo para simplificar lo que se manda a la ui para mostrar el horario de las rutas
data class RouteInfoSchedulel(
    val id: String,
    val name: String,
    val outboundInfo: Schedule,
    val inboundInfo: Schedule,
    //val schedulesInfo: List<Schedule>,
)

/**
 * Transforma los datos de las rutas en una lista de modelos de ui
 */
fun getSampleRoutesSchedule(): List<RouteInfoSchedulel> {
    return sampleRoutes.map { route ->
        // trayecto de ida
        val outboundOriginName = route.outboundJourney.stops.firstOrNull()?.name ?: "N/A"
        val outboundDestinationName = route.outboundJourney.stops.lastOrNull()?.name ?: "N/A"
        val outboundSchedule =
            Pair(
                route.outboundJourney.firstDeparture,
                route.outboundJourney.lastDeparture
            )

        // trayecto de vuelta
        val inboundOriginName = route.inboundJourney.stops.firstOrNull()?.name ?: "N/A"
        val inboundDestinationName = route.inboundJourney.stops.lastOrNull()?.name ?: "N/A"
        val inboundSchedule =
            Pair(
                route.inboundJourney.firstDeparture,
                route.inboundJourney.lastDeparture
            )


        val outboundInfo = Schedule(
            nameOrigin = outboundOriginName,
            nameDestination = outboundDestinationName,
            schedule = outboundSchedule
        )

        val inboundDataInfo = Schedule(
            nameOrigin = inboundOriginName,
            nameDestination = inboundDestinationName,
            schedule = inboundSchedule
        )


        RouteInfoSchedulel(
            id = route.id,
            name = route.name,
            outboundInfo = outboundInfo,
            inboundInfo = inboundDataInfo
            //schedulesInfo = listOf(outboundInfo, inboundDataInfo)
        )
    }
}