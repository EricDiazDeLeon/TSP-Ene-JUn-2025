package com.example.mirutadigital.ui.screens.routes.allRoutes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.testsUi.dataSource.RoutesInfo
import com.example.mirutadigital.data.testsUi.dataSource.JourneyInfo
import com.example.mirutadigital.data.testsUi.dataSource.getSampleStopsWithRoutes
import com.example.mirutadigital.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.google.maps.android.PolyUtil

/**
 * define el estado de la ui para AllRoutesScreen
 * encapsula propiedades que el composable necesita para funcionar
 */
data class AllRoutesUiState(
    val routes: List<RoutesInfo> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val filteredStopId: String? = null,
    val filteredStopName: String? = null
) {
    val filteredRoutes: List<RoutesInfo>
        get() {
            var result = routes

            if (filteredStopId != null) {
                result = result.filter { route ->
                    route.stopsJourney.any { journey ->
                        journey.stops.any { stop -> stop.id == filteredStopId }
                    }
                }
            }

            if (searchQuery.isNotBlank()) {
                result = result.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }

            return result
        }
}

class AllRoutesViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllRoutesUiState())
    val uiState: StateFlow<AllRoutesUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
    }

    // carga las rutas desde el repositorio
    private fun loadRoutes() {
        viewModelScope.launch {
            try {
                // Obtener rutas desde el repositorio
                val repositoryRoutes = repository.getRoutes().first()
                // Obtener paradas para asociarlas a cada ruta
                val repositoryStops = repository.getStops().first()
                
                // Convertir Route (repo) a RoutesInfo (UI)
                val routes = repositoryRoutes.map { route ->
                    // Paradas asociadas a la ruta
                    val routeStopsData = repositoryStops.filter { stop ->
                        stop.associatedRouteIds.contains(route.id)
                    }

                    val uiStops = routeStopsData.map { stop ->
                        com.example.mirutadigital.data.testsUi.model.Stop(
                            id = stop.id,
                            name = stop.name,
                            coordinates = stop.location
                        )
                    }

                    // Polilínea codificada para dibujar (si hay puntos)
                    val encodedPolyline = if (route.polylinePoints.isNotEmpty()) {
                        PolyUtil.encode(route.polylinePoints)
                    } else null

                    val outbound = JourneyInfo(
                        stops = uiStops,
                        encodedPolyline = encodedPolyline
                    )
                    // Sin trayecto diferenciado aún en Firestore: usando el mismo orden invertido para “vuelta”
                    val inbound = JourneyInfo(
                        stops = uiStops.reversed(),
                        encodedPolyline = encodedPolyline
                    )

                    RoutesInfo(
                        id = route.id,
                        name = route.name,
                        windshieldLabel = "No Tiene",
                        colors = "Sin Especificar",
                        stopsJourney = listOf(outbound, inbound)
                    )
                }
                
                _uiState.update {
                    it.copy(
                        routes = routes,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // En caso de error, mantener el estado de carga
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }


    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setStopFilter(stopId: String) {
        viewModelScope.launch {
            // Usar paradas reales del repositorio para obtener el nombre
            val stops = repository.getStops().first()
            val stopName = stops.find { it.id == stopId }?.name

            _uiState.update {
                it.copy(
                    filteredStopId = stopId,
                    filteredStopName = stopName
                )
            }
        }
    }

    fun clearStopFilter() {
        _uiState.update {
            it.copy(
                filteredStopId = null,
                filteredStopName = null
            )
        }
    }
}
