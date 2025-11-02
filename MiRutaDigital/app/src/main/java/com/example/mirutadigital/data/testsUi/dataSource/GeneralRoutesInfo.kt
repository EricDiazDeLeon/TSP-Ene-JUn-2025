package com.example.mirutadigital.data.testsUi.dataSource

import com.example.mirutadigital.data.testsUi.model.Stop
import com.example.mirutadigital.data.testsUi.sampleRoutes

// allroutes
data class JourneyInfo(
    val stops: List<Stop>,
    val encodedPolyline: String? = null
)

// modelo para simplificar lo que se manda a la ui para mostrar el horario de las rutas
data class RoutesInfo(
    val id: String,
    val name: String,
    val windshieldLabel: String,
    val colors: String,
    val stopsJourney: List<JourneyInfo>,
    val isFavorite: Boolean = false
)

/**
 * Transforma los datos de las rutas en una lista de modelos de ui
 */
fun getSampleRoutes(): List<RoutesInfo> {
    return sampleRoutes.map { route ->
        val windshieldLabel = route.detail?.windshieldLabel ?: "No Tiene"
        val colors = route.detail?.colors ?: "Sin Especificar"

        val stopsJourney = listOf(
            JourneyInfo(
                stops = route.outboundJourney.stops,
                encodedPolyline = route.outboundJourney.encodedPolyline
            ),
            JourneyInfo(
                stops = route.inboundJourney.stops,
                encodedPolyline = route.inboundJourney.encodedPolyline
            )
        )

        RoutesInfo(
            id = route.id,
            name = route.name,
            windshieldLabel = windshieldLabel,
            colors = colors,
            stopsJourney = stopsJourney
        )
    }
}
