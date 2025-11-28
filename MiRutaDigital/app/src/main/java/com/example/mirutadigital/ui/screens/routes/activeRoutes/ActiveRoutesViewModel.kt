package com.example.mirutadigital.ui.screens.routes.activeRoutes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.MiRutaApplication
import com.example.mirutadigital.data.model.ui.RouteInfoSchedulel
import com.example.mirutadigital.data.model.ui.RoutesInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * define el estado de la ui para ActiveRoutesScreen
 * encapsula propiedades que el composable necesita para funcionar
 */
data class ActiveRoutesUiState(
    val routes: List<RouteInfoSchedulel> = emptyList(),
    val favoriteRouteIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val currentTime: String = "",
    val showOnlyFavorites: Boolean = false
) {
    val filteredRoutes: List<RouteInfoSchedulel>
        get() {
            // rutas activas
            val activeRoutes = routes.filter { route ->
                isRouteActive(route, currentTime)
            }

            var result = activeRoutes

            if (showOnlyFavorites) {
                result = result.filter { it.id in favoriteRouteIds }
            }

            // filtro por busqueda
            if (searchQuery.isBlank()) {
                activeRoutes.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }

            return result
        }
}

class ActiveRoutesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiRutaApplication).repository

    private val _uiState = MutableStateFlow(ActiveRoutesUiState())
    val uiState: StateFlow<ActiveRoutesUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
        updateCurrentTime()
    }

    // carga las rutas desde la fuente de datos
    private fun loadRoutes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                //repository.synchronizeDatabase()

                val routes = repository.getRoutesSchedule()// getSampleRoutesSchedule

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

    suspend fun getFullRouteInfo(routeId: String): RoutesInfo? {
        val allRoutes = repository.getGeneralRoutesInfo() // getSampleRoutes
        return allRoutes.find { it.id == routeId }
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

    private fun updateCurrentTime() {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(calendar.time)

        _uiState.update { it.copy(currentTime = currentTime) }
    }
}

// devuelve true si la ruta esta activa segun la hora actual
private fun isRouteActive(route: RouteInfoSchedulel, currentTime: String): Boolean {
    val outboundActive = isTimeInRange(
        currentTime,
        route.outboundInfo.schedule.first,
        route.outboundInfo.schedule.second
    )

    val inboundActive = isTimeInRange(
        currentTime,
        route.inboundInfo.schedule.first,
        route.inboundInfo.schedule.second
    )

    return outboundActive || inboundActive
}

// devuelve true si la hora actual esta dentro del rango de inicio y fin
private fun isTimeInRange(current: String, start: String, end: String): Boolean {
    try {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val currentTime = timeFormat.parse(current)
        val startTime = timeFormat.parse(start)
        val endTime = timeFormat.parse(end)

        if (currentTime == null || startTime == null || endTime == null) {
            return false
        }

        return currentTime.time in startTime.time..endTime.time
    } catch (_: Exception) {
        return false
    }
}