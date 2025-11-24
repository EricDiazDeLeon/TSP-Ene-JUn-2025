package com.example.mirutadigital.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.MiRutaApplication
import com.example.mirutadigital.data.model.ui.RoutesInfo
import com.example.mirutadigital.data.model.ui.StopWithRoutes
import com.example.mirutadigital.data.model.ui.base.Stop
import com.example.mirutadigital.data.repository.ActiveShareData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class MapDisplayMode {
    data class AllStops(val focusedStopId: String? = null) : MapDisplayMode()

    data class RouteDetail(
        val routeId: String,
        val outboundPolyline: List<LatLng>,
        val inboundPolyline: List<LatLng>,
        val routeStops: List<Stop>,
        val bounds: LatLngBounds
    ) : MapDisplayMode()

    data class SharedVehicles(
        val routeId: String,
        val vehicles: List<ActiveShareData>,
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
    val isLoading: Boolean = true,
    val currentRouteName: String? = null
)

class MapStateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiRutaApplication).repository

    val initialLocation = LatLng(22.7626, -102.5807)

    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    init {
        loadMapData()
    }

    private fun loadMapData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.synchronizeDatabase()

                _mapState.update { it.copy(isLoading = true) }

                val stops = repository.getStopsWithRoutes() //getSampleStopsWithRoutes

                withContext(Dispatchers.Main) {
                    _mapState.update {
                        it.copy(
                            allStops = stops,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                // error
            }
        }
    }

    fun setSelectedStop(stopId: String?) {
        _mapState.update {
            it.copy(selectedStopId = stopId)
        }
    }

    fun showAllStops() {
        if (_mapState.value.allStops.isEmpty()) {
            loadMapData()
        }

        _mapState.update {
            it.copy(
                displayMode = MapDisplayMode.AllStops(null),
                selectedStopId = null,
                currentRouteName = null
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

    /**
     * Esta funcion la usa MainScreen cuando navega a la
     * pantalla de detalle y solo tiene el ID
     */
    fun showRouteDetailById(routeId: String) {
        viewModelScope.launch {
            val route = repository.getGeneralRoutesInfo().find { it.id == routeId }
            if (route != null) {
                showRouteDetailFromInfo(route)
            }
        }
    }

    /**
     * Esta funcion la usan AllRoutesScreen y ActiveRoutesScreen
     * cuando ya tienen la informacion de la ruta
     */
    fun showRouteDetailFromInfo(route: RoutesInfo) {
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
            inboundStops.map { it.coordinates }
        } else {
            PolyUtil.decode(inboundEncodedPoly)
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
                selectedStopId = null,
                currentRouteName = "ruta " + route.name
            )
        }
    }

    /**
     * Obtiene los vehículos compartidos del repositorio y
     * actualiza el estado del mapa para mostrarlos.
     */
    fun showSharedVehiclesForRoute(routeId: String) {
        viewModelScope.launch {
            _mapState.update { it.copy(isLoading = true) }

            val routeInfo = repository.getGeneralRoutesInfo().find { it.id == routeId }
            val activeShares = repository.getActiveSharesForRoute(routeId)

            if (routeInfo == null) {
                _mapState.update { it.copy(isLoading = false) }
                return@launch
            }

            val outboundStops = routeInfo.stopsJourney.getOrNull(0)?.stops ?: emptyList()
            val inboundStops = routeInfo.stopsJourney.getOrNull(1)?.stops ?: emptyList()
            val allRouteStops = (outboundStops + inboundStops).distinctBy { it.id }

            val outboundEncodedPoly = routeInfo.stopsJourney.getOrNull(0)?.encodedPolyline
            val inboundEncodedPoly = routeInfo.stopsJourney.getOrNull(1)?.encodedPolyline

            val outboundPoly = PolyUtil.decode(outboundEncodedPoly ?: "")
            val inboundPoly = PolyUtil.decode(inboundEncodedPoly ?: "")

            val boundsBuilder = LatLngBounds.Builder()
            (outboundPoly + inboundPoly).forEach { boundsBuilder.include(it) }
            allRouteStops.forEach { boundsBuilder.include(it.coordinates) } // Incluir paradas en bounds

            val routeBounds =
                if (outboundPoly.isNotEmpty() || inboundPoly.isNotEmpty() || allRouteStops.isNotEmpty()) {
                    boundsBuilder.build()
                } else {
                    LatLngBounds.Builder().include(initialLocation).build()
                }

            _mapState.update {
                it.copy(
                    displayMode = MapDisplayMode.SharedVehicles(
                        routeId = routeId,
                        vehicles = activeShares,
                        outboundPolyline = outboundPoly,
                        inboundPolyline = inboundPoly,
                        routeStops = allRouteStops,
                        bounds = routeBounds
                    ),
                    selectedStopId = null,
                    currentRouteName = routeInfo.name,
                    isLoading = false
                )
            }
        }
    }
}
