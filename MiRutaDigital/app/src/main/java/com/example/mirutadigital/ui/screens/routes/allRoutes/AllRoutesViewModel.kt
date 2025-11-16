package com.example.mirutadigital.ui.screens.routes.allRoutes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.ViewModel

import com.example.mirutadigital.MiRutaApplication
import com.example.mirutadigital.data.model.ui.RoutesInfo
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers

//import com.example.mirutadigital.data.testsUi.dataSource.RoutesInfo
//import com.example.mirutadigital.data.testsUi.dataSource.getSampleRoutes
//import com.example.mirutadigital.data.testsUi.dataSource.getSampleStopsWithRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * define el estado de la ui para AllRoutesScreen
 * encapsula propiedades que el composable necesita para funcionar
 */
data class AllRoutesUiState(
    val routes: List<RoutesInfo> = emptyList(),
    val favoriteRouteIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val filteredStopId: String? = null,
    val filteredStopName: String? = null,
    val showOnlyFavorites: Boolean = false
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

            if(showOnlyFavorites) {
                result = result.filter { it.id in favoriteRouteIds }
            }

            if (searchQuery.isNotBlank()) {
                result = result.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }

            return result
        }
}

class AllRoutesViewModel(application: Application) : AndroidViewModel(application)  {

    private val repository = (application as MiRutaApplication).repository

    private val _uiState = MutableStateFlow(AllRoutesUiState())
    val uiState: StateFlow<AllRoutesUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
    }

    // carga las rutas desde la fuente de datos
    private fun loadRoutes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.synchronizeDatabase()

                val routes = repository.getGeneralRoutesInfo()// getSampleRoutes

                withContext(Dispatchers.Main) {
                    repository.getAllFavorites().collect { favorites ->
                        val favoriteIds = favorites.map { it.routeId }.toSet()
                        _uiState.update {
                            it.copy(
                                routes = routes,
                                favoriteRouteIds = favoriteIds,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
               // error
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setStopFilter(stopId: String) {
        viewModelScope.launch {
            val stops = repository.getStopsWithRoutes() // getSampleStopsWithRoutes
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

    fun toggleShowOnlyFavorites() {
        _uiState.update { it.copy(showOnlyFavorites = !it.showOnlyFavorites) }
    }

    fun toggleFavorite(routeId: String) {
        viewModelScope.launch {
            val isFavorite = routeId in _uiState.value.favoriteRouteIds
            repository.toggleFavorite(routeId, isFavorite)
        }
    }

    fun isFavorite(routeId: String): Boolean {
        return routeId in _uiState.value.favoriteRouteIds
    }
}
