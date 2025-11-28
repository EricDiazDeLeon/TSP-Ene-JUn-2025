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
    open val bounds: LatLngBounds? = null

    data class AllStops(val focusedStopId: String? = null) : MapDisplayMode()

    data class RouteDetail(
        val routeId: String,
        val outboundPolyline: List<LatLng>,
        val inboundPolyline: List<LatLng>,
        val routeStops: List<Stop>,
        override val bounds: LatLngBounds?
    ) : MapDisplayMode()

    data class SharedVehicles(
        val routeId: String,
        val bus: List<ActiveShareData>,
        val outboundPolyline: List<LatLng>,
        val inboundPolyline: List<LatLng>,
        val routeStops: List<Stop>,
        override val bounds: LatLngBounds?
    ) : MapDisplayMode()
}

// Modos de selección para TripPlanScreen
enum class SelectionMode {
    NONE,
    ORIGIN,
    DESTINATION
}

// el estado del mapa que se comparte
data class MapState(
    val allStops: List<StopWithRoutes> = emptyList(),
    val selectedStopId: String? = null,
    val displayMode: MapDisplayMode = MapDisplayMode.AllStops(null),
    val isLoading: Boolean = true,
    val currentRouteName: String? = null,
    // Nuevos campos para la selección
    val selectionMode: SelectionMode = SelectionMode.NONE,
    val temporaryPoint: LatLng? = null,
    val confirmedOrigin: LatLng? = null,
    val confirmedDestination: LatLng? = null
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
            _mapState.update { it.copy(isLoading = true) }

            var stops = try {
                repository.getStopsWithRoutes()
            } catch (e: Exception) {
                emptyList()
            }

            if (stops.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    _mapState.update { it.copy(allStops = stops) }
                }
            }

            if (stops.isEmpty()) {
                try {
                    repository.synchronizeDatabase()
                    stops = repository.getStopsWithRoutes()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            withContext(Dispatchers.Main) {
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

    // --- Métodos para la selección de puntos ---

    fun setSelectionMode(mode: SelectionMode) {
        _mapState.update {
            it.copy(
                selectionMode = mode,
                temporaryPoint = null, // Limpiar punto temporal anterior
                selectedStopId = null  // Limpiar selección de parada previa
            )
        }
    }

    fun setTemporaryPoint(latLng: LatLng) {
        if (_mapState.value.selectionMode != SelectionMode.NONE) {
            _mapState.update { it.copy(temporaryPoint = latLng) }
        }
    }

    fun confirmSelection() {
        _mapState.update {
            val point = it.temporaryPoint ?: return@update it

            when (it.selectionMode) {
                SelectionMode.ORIGIN -> it.copy(
                    confirmedOrigin = point,
                    selectionMode = SelectionMode.NONE,
                    temporaryPoint = null
                )
                SelectionMode.DESTINATION -> it.copy(
                    confirmedDestination = point,
                    selectionMode = SelectionMode.NONE,
                    temporaryPoint = null
                )
                else -> it
            }
        }
    }

    fun cancelSelection() {
        _mapState.update {
            it.copy(
                selectionMode = SelectionMode.NONE,
                temporaryPoint = null
            )
        }
    }
    
    fun clearPlanPoints() {
        _mapState.update {
            it.copy(
                confirmedOrigin = null,
                confirmedDestination = null,
                selectionMode = SelectionMode.NONE,
                temporaryPoint = null
            )
        }
    }

    // -------------------------------------------

    fun showAllStops() {
        if (_mapState.value.allStops.isEmpty()) {
            loadMapData()
        }

        _mapState.update {
            it.copy(
                displayMode = MapDisplayMode.AllStops(null),
                selectedStopId = null,
                currentRouteName = null,
                // Limpiamos el modo seleccion al salir de TripPlanScreen
                selectionMode = SelectionMode.NONE,
                temporaryPoint = null
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

    fun showRouteDetailById(routeId: String) {
        viewModelScope.launch {
            val route = repository.getGeneralRoutesInfo().find { it.id == routeId }
            if (route != null) {
                showRouteDetailFromInfo(route)
            }
        }
    }

    fun showRouteDetailFromInfo(route: RoutesInfo) {
        val outboundStops = route.stopsJourney.getOrNull(0)?.stops ?: emptyList()
        val inboundStops = route.stopsJourney.getOrNull(1)?.stops ?: emptyList()
        val allRouteStops = (outboundStops + inboundStops).distinctBy { it.id }

        val outboundEncodedPoly = route.stopsJourney.getOrNull(0)?.encodedPolyline
        val inboundEncodedPoly = route.stopsJourney.getOrNull(1)?.encodedPolyline

        val outboundPoly = if (outboundEncodedPoly.isNullOrBlank()) {
            outboundStops.map { it.coordinates }
        } else {
            try {
                PolyUtil.decode(outboundEncodedPoly)
            } catch (e: Exception) {
                outboundStops.map { it.coordinates }
            }
        }

        val inboundPoly = if (inboundEncodedPoly.isNullOrBlank()) {
            inboundStops.map { it.coordinates }
        } else {
            try {
                PolyUtil.decode(inboundEncodedPoly)
            } catch (e: Exception) {
                inboundStops.map { it.coordinates }
            }
        }

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
                currentRouteName = route.name
            )
        }
    }

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

            val outboundPoly = try {
                PolyUtil.decode(outboundEncodedPoly ?: "")
            } catch (e: Exception) {
                outboundStops.map { it.coordinates }
            }

            val inboundPoly = try {
                PolyUtil.decode(inboundEncodedPoly ?: "")
            } catch (e: Exception) {
                inboundStops.map { it.coordinates }
            }

            val boundsBuilder = LatLngBounds.Builder()
            (outboundPoly + inboundPoly).forEach { boundsBuilder.include(it) }
            allRouteStops.forEach { boundsBuilder.include(it.coordinates) }

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
                        bus = activeShares,
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
