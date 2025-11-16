package com.example.mirutadigital.testsUi.dataSource

import com.example.mirutadigital.testsUi.sampleRoutes

// el modelos y logica que necesita la interfaz para mostrar
data class StopWithRoutes(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val routes: List<RouteInfo>,
    val distance: String = "Calculando..."
)

// modelo de resumen de ruta para mostrarlo en la lista
data class RouteInfo(
    val name: String,
    val destination: String // el destino de la ruta desde esa parada
)

/**
 * la funcoin procesa los datos para que lleguen a la vista adaptados,
 * lee los datos de ejemplo (sampleRoutes y sampleStops) y los transforma
 * en una lista de paradas para ser mostradas en la interfaz
 */
fun getSampleStopsWithRoutes(): List<StopWithRoutes> {
    val stopWithRoutesList = mutableListOf<StopWithRoutes>()

    // obtiene todas las paradas unicas de todas las rutas
    val allStops = sampleRoutes.flatMap { it.outboundJourney.stops + it.inboundJourney.stops }
        .distinctBy { it.id }

    // itera sobre cada parada para encontrar que rutas pasan por ella
    for ((_, stop) in allStops.withIndex()) {
        val routesForStop = sampleRoutes.filter { route ->
            route.outboundJourney.stops.any { it.id == stop.id } || route.inboundJourney.stops.any { it.id == stop.id }
        }

        // para cada ruta encontrada, determina el destino final para mostrarlo en la vista
        val routeInfo = routesForStop.map { route ->
            val isOutbound = route.outboundJourney.stops.any { it.id == stop.id }
            val destination = if (isOutbound) {
                route.outboundJourney.stops.last().name // destino del viaje de ida
            } else {
                route.inboundJourney.stops.last().name //destino del viaje de vuelta
            }
            RouteInfo(route.name, destination) // crea clase resumen de ruta
        }

        // crea el objeto (StopWithRoutes) con toda la informacion combinada para mostrar en la vista
        stopWithRoutesList.add(
            StopWithRoutes(
                id = stop.id,
                name = stop.name,
                latitude = stop.coordinates.latitude,
                longitude = stop.coordinates.longitude,
                routes = routeInfo
            )
        )
    }

    // nunca deberia de entrar por que simepre hay rutas para las paradas
    if (stopWithRoutesList.isEmpty()) {
        stopWithRoutesList.add(
            StopWithRoutes(
                id = "no_stops",
                name = "Ya no hay paradas",
                latitude = 0.0,
                longitude = 0.0,
                routes = emptyList()
            )
        )
    }

    return stopWithRoutesList // envia a la vista las paradas adaptadas
}