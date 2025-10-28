package com.example.mirutadigital.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.testsUi.dataSource.StopWithRoutes
import com.example.mirutadigital.data.testsUi.dataSource.RouteInfo
import com.example.mirutadigital.data.repository.AppRepository
import com.example.mirutadigital.data.model.Stop
import kotlinx.coroutines.launch
import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first

/**
 * define el estado de la ui para StopsSheetContent
 * encapsula propiedades que el composable necesita para funcionar
 * que antes se manejaban dentro de el
 */
data class StopsUiState(
    val allStops: List<StopWithRoutes> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val userLocation: Location? = null
) {
    // propiedad que se calcula y que devuelve la lista de paradas filtradas
    val filteredStops: List<StopWithRoutes>
        get() = if (searchQuery.isBlank()) {
            allStops
        } else {
            allStops.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
}

class StopsViewModel(
    private val repository: AppRepository
) : ViewModel() {

    // estado privado que contiene toda la información de la ui
    private val _uiState = MutableStateFlow(StopsUiState())
    // estado publico de solo lectura para que la ui lo observe
    val uiState: StateFlow<StopsUiState> = _uiState.asStateFlow()

    init {
        loadStopsData()
    }

    // carga la lista inicial de paradas desde Firestore
    private fun loadStopsData() {
        viewModelScope.launch {
            try {
                // Sincronizar con Firestore para asegurar datos en Room
                repository.refreshStops()
                repository.refreshRoutes()
                // Obtener paradas desde el repositorio
                val stops = repository.getStops().first()
                val routes = repository.getRoutes().first()
                
                // Convertir Stop a StopWithRoutes
                val stopsWithRoutes = stops.map { stop ->
                    val associatedRoutes = routes.filter { route ->
                        stop.associatedRouteIds.contains(route.id)
                    }

                    StopWithRoutes(
                        id = stop.id,
                        name = stop.name,
                        latitude = stop.location.latitude,
                        longitude = stop.location.longitude,
                        // Mapeamos a RouteInfo (nombre + destino). Sin datos de destino en Firestore,
                        // usamos un marcador genérico por ahora.
                        routes = associatedRoutes.map { route ->
                            RouteInfo(
                                name = route.name,
                                destination = "Destino no disponible"
                            )
                        }
                    )
                }
                
                _uiState.update {
                    it.copy(
                        allStops = stopsWithRoutes,
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

    // funcion publica que llama la ui cuando se escribe en la barra de busqueda
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query)
        }
    }

    fun updateAndSortByLocation(location: Location?) {
        _uiState.update { currentState ->
            if (location == null) {
                return@update currentState
            }

            val sortedStops = currentState.allStops.sortedBy { stop ->
                val stopLocation = Location("").apply {
                    latitude = stop.latitude
                    longitude = stop.longitude
                }
                location.distanceTo(stopLocation)
            }

            currentState.copy(allStops = sortedStops, userLocation = location)
        }
    }
}
