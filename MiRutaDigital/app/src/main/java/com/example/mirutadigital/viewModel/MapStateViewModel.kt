package com.example.mirutadigital.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.repository.AppRepository
import com.example.mirutadigital.data.testsUi.dataSource.RoutesInfo
import com.example.mirutadigital.data.testsUi.dataSource.StopWithRoutes
import com.example.mirutadigital.data.testsUi.dataSource.getSampleStopsWithRoutes
import com.example.mirutadigital.data.testsUi.model.Stop
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.maps.android.PolyUtil

sealed class MapDisplayMode {
    data class AllStops(val focusedStopId: String? = null) : MapDisplayMode()

    data class RouteDetail(
        val routeId: String,
        val outboundPolyline: List<LatLng>,
        val inboundPolyline: List<LatLng>,
        val routeStops: List<Stop>,
        val bounds: LatLngBounds
    ) : MapDisplayMode()
}

// el estado del mapa que se comparte
data class MapState(
    val allStops: List<StopWithRoutes> = emptyList(),
    val selectedStopId: String? = null,
    val displayMode: MapDisplayMode = MapDisplayMode.AllStops(null),
    val isLoading: Boolean = true
)

class MapStateViewModel(
    private val repository: AppRepository? = null
) : ViewModel() {

    val initialLocation = LatLng(22.7626, -102.5807)

    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    init {
        loadMapData()
    }

    private fun loadMapData() {
        viewModelScope.launch {
            try {
                if (repository != null) {
                    // Sincronizar con Firestore primero
                    repository.refreshStops()
                    repository.refreshRoutes()
                    
                    // Obtener datos reales
                    val stops = repository.getStops().first()
                    val routes = repository.getRoutes().first()
                    
                    // Mapear a StopWithRoutes para compatibilidad con UI
                    val stopsWithRoutes = stops.map { stop ->
                        val associatedRoutes = stop.associatedRouteIds.mapNotNull { routeId ->
                            routes.find { it.id == routeId }
                        }.map { route ->
                             com.example.mirutadigital.data.testsUi.dataSource.RouteInfo(
                                 name = route.name,
                                 destination = "Destino no disponible" // Placeholder hasta que tengamos destinos reales
                             )
                         }
                        
                        StopWithRoutes(
                            id = stop.id,
                            name = stop.name,
                            latitude = stop.location.latitude,
                            longitude = stop.location.longitude,
                            routes = associatedRoutes
                        )
                    }
                    
                    _mapState.update {
                        it.copy(
                            allStops = stopsWithRoutes,
                            isLoading = false
                        )
                    }
                } else {
                    // Fallback a datos de muestra si no hay repositorio
                    val stops = getSampleStopsWithRoutes()
                    _mapState.update {
                        it.copy(
                            allStops = stops,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                // En caso de error, usar datos de muestra
                val stops = getSampleStopsWithRoutes()
                _mapState.update {
                    it.copy(
                        allStops = stops,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setSelectedStop(stopId: String?) {
        _mapState.update {
            it.copy(selectedStopId = stopId)
        }
    }

    fun showAllStops() {
        _mapState.update {
            it.copy(
                displayMode = MapDisplayMode.AllStops(null),
                selectedStopId = null
            )
        }
    }

    fun showSingleStopFocus(stopId: String) {
        _mapState.update {
            it.copy(
                displayMode = MapDisplayMode.AllStops(focusedStopId = stopId),
                selectedStopId = stopId
            )
        }
    }

    fun showRouteDetail(route: RoutesInfo) {
        // paradas de los trayectos ida y vuelta
        val outboundStops = route.stopsJourney.getOrNull(0)?.stops ?: emptyList()
        val inboundStops = route.stopsJourney.getOrNull(1)?.stops ?: emptyList()
        val allRouteStops = (outboundStops + inboundStops).distinctBy { it.id }


        val outboundEncodedPoly = route.stopsJourney.getOrNull(0)?.encodedPolyline
        val inboundEncodedPoly = route.stopsJourney.getOrNull(1)?.encodedPolyline


        val outboundPoly = if (outboundEncodedPoly.isNullOrBlank()) {
            outboundStops.map { it.coordinates } // si no hay polilinea creamos una con las paradas
        } else {
            PolyUtil.decode(outboundEncodedPoly) // decodificamos
        }

        val inboundPoly = if (inboundEncodedPoly.isNullOrBlank()) {
            inboundStops.map { it.coordinates } // si no hay polilinea creamos una con las paradas
        } else {
            PolyUtil.decode(inboundEncodedPoly) // decodificamos
        }

        // creamos los limites para centrar el mapa
        val boundsBuilder = LatLngBounds.Builder()
        var hasPoints = false

        (outboundPoly + inboundPoly).forEach {
            boundsBuilder.include(it)
            hasPoints = true
        }

        allRouteStops.forEach {
            boundsBuilder.include(it.coordinates)
            hasPoints = true
        }

        val routeBounds = if (hasPoints) {
            boundsBuilder.build()
        } else {
            LatLngBounds.Builder().include(initialLocation).build()
        }

        _mapState.update {
            it.copy(
                displayMode = MapDisplayMode.RouteDetail(
                    routeId = route.id,
                    outboundPolyline = outboundPoly,
                    inboundPolyline = inboundPoly,
                    routeStops = allRouteStops,
                    bounds = routeBounds
                ),
                selectedStopId = null
            )
        }
    }
}