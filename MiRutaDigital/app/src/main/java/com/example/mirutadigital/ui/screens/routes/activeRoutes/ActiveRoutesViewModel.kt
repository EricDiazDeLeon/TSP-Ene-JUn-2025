package com.example.mirutadigital.ui.screens.routes.activeRoutes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.testsUi.dataSource.RouteInfoSchedulel
import com.example.mirutadigital.data.testsUi.dataSource.RoutesInfo
import com.example.mirutadigital.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.first

/**
 * define el estado de la ui para ActiveRoutesScreen
 * encapsula propiedades que el composable necesita para funcionar
 */
data class ActiveRoutesUiState(
    val routes: List<RouteInfoSchedulel> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val currentTime: String = ""
) {
    val filteredRoutes: List<RouteInfoSchedulel>
        get() {
            // rutas activas
            val activeRoutes = routes.filter { route ->
                isRouteActive(route, currentTime)
            }

            // filtro por busqueda
            return if (searchQuery.isBlank()) {
                activeRoutes
            } else {
                activeRoutes.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
        }
}

class ActiveRoutesViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveRoutesUiState())
    val uiState: StateFlow<ActiveRoutesUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
        updateCurrentTime()
    }

    // carga las rutas desde el repositorio
    private fun loadRoutes() {
        viewModelScope.launch {
            try {
                // Obtener rutas desde el repositorio
                val repositoryRoutes = repository.getRoutes().first()
                
                // Convertir Route a RouteInfoSchedulel
                // Por ahora usamos datos de muestra, ya que Route no tiene toda la info de RouteInfoSchedulel
                // TODO: Adaptar completamente el modelo Route a RouteInfoSchedulel o crear un adaptador
                val routes = listOf<RouteInfoSchedulel>() // Por ahora vacío, necesitamos implementar la conversión
                
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

    fun getFullRouteInfo(routeId: String): RoutesInfo? {
        // TODO: Implementar obtención de información completa de la ruta desde el repositorio
        return null
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