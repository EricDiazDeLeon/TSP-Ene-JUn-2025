package com.example.mirutadigital.data.pruebasUi

import com.example.mirutadigital.data.pruebasUi.model.route.Journey
import com.example.mirutadigital.data.pruebasUi.model.route.Route
import com.example.mirutadigital.data.pruebasUi.model.Stop
import com.google.android.gms.maps.model.LatLng

// lista de paradas de ejemplo
val sampleStops = listOf(
    Stop("plaza_alesia", "Plaza Alesia", LatLng(22.76, -102.58)),
    Stop("miguel_aleman", "Miguel Alemán", LatLng(22.77, -102.57)),
    Stop("mina_patrocinio_140", "Mina del Patrocinio, 140", LatLng(22.78, -102.56)),
    Stop("sauceda", "Jardines de Sauceda", LatLng(22.75, -102.59)),
    Stop("villa_fontana", "Villa Fontana", LatLng(22.768423525502666, -102.4805096358822)),
    Stop("polideportivo", "Polideportivo", LatLng(22.76, -102.55))
)

// lista de rutas de ejemplo
val sampleRoutes = listOf(
    Route(
        id = "R_3",
        name = "Ruta 3",
        price = 10.0,
        departureInterval = 15,
        outboundJourney = Journey(
            stops = listOf(
                sampleStops[0],
                sampleStops[1],
                sampleStops[5]
            ), // ida: Alesia -> Alemán -> Polideportivo
            firstDeparture = "06:00",
            lastDeparture = "22:00"
        ),
        inboundJourney = Journey(
            stops = listOf(
                sampleStops[5],
                sampleStops[1],
                sampleStops[0]
            ), // vuelta: Polideportivo -> Alemán -> Alesia
            firstDeparture = "06:30",
            lastDeparture = "22:30"
        )
    ),
    Route(
        id = "R_16",
        name = "Ruta 16",
        price = 9.5,
        departureInterval = 20,
        outboundJourney = Journey(
            stops = listOf(sampleStops[0], sampleStops[3]), // Ida: Alesia -> Sauceda
            firstDeparture = "05:30",
            lastDeparture = "21:30"
        ),
        inboundJourney = Journey(
            stops = listOf(sampleStops[3], sampleStops[0]), // Vuelta: Sauceda -> Alesia
            firstDeparture = "06:00",
            lastDeparture = "22:00"
        )
    ),
    Route(
        id = "R_17",
        name = "Ruta 17",
        price = 10.5,
        departureInterval = 25,
        outboundJourney = Journey(
            stops = listOf(sampleStops[0], sampleStops[4]), // ida: Alesia -> Villa Fontana
            firstDeparture = "06:20",
            lastDeparture = "21:00"
        ),
        inboundJourney = Journey(
            stops = listOf(sampleStops[4], sampleStops[0]), // vuelta: Villa Fontana -> Alesia
            firstDeparture = "06:05",
            lastDeparture = "21:30"
        )
    )
)


// El modelos y logica que necesita la interfaz para mostrar

// modelo especifico que convina datos para tener una parada con todas las rutas que pasan por ella
// y envie la distancia antes de cargar la pantalla
data class StopWithRoutes(
    val id: String,
    val name: String,
    val distance: String,
    val routes: List<RouteInfo>
)

// modelo de resumen de ruta para mostrarlo en la lista
data class RouteInfo(
    val name: String,
    val destination: String // el destino de la ruta desde esa parada
)

/**
] * la funcoin procesa los datos para que lleguen a la vista adaptados,
 * lee los datos de ejemplo (sampleRoutes y sampleStops) y los transforma
 * en una lista de paradas adaptadas para ser mostradas en la interfaz
 */
fun getSampleStopsWithRoutes(): List<StopWithRoutes> {
    val stopWithRoutesList = mutableListOf<StopWithRoutes>()

    // obtiene todas las paradas unicas de todas las rutas
    val allStops = sampleRoutes.flatMap { it.outboundJourney.stops + it.inboundJourney.stops }.distinctBy { it.id }

    // itera sobre cada parada para encontrar que rutas pasan por ella
    for ((index, stop) in allStops.withIndex()) {
        val routesForStop = sampleRoutes.filter { route ->
            route.outboundJourney.stops.any { it.id == stop.id } || route.inboundJourney.stops.any { it.id == stop.id }
        }

        // para cada ruta encontrada, determina el destino final para mostrarlo en la vista
        val routeInfos = routesForStop.map { route ->
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
                distance = "Desde tu ubicación a ${50 + index * 35}m", // Distancia de ejemplo. //cambiar
                routes = routeInfos
            )
        )
    }

    // nunca deberia de entrar por que simepre hay rutas para las paradas
    if (stopWithRoutesList.isEmpty()) {
        stopWithRoutesList.add(
            StopWithRoutes(
                id = "no_stops",
                name = "Ya no hay paradas",
                distance = "lo sentimos",
                routes = emptyList()
            )
        )
    }

    return stopWithRoutesList // envia a la vista las paradas adaptadas
}

