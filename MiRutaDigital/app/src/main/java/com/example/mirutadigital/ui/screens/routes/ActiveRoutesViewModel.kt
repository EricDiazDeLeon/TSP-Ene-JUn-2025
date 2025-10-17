package com.example.mirutadigital.ui.screens.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.testsUi.RouteInfoSchedulel
import com.example.mirutadigital.data.testsUi.getSampleRoutesSchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * define el estado de la ui para ActiveRoutesScreen
 * encapsula propiedades que el composable necesita para funcionar
 */
data class RoutesUiState(
    val routes: List<RouteInfoSchedulel> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
) {
    val filteredRoutes: List<RouteInfoSchedulel>
        get() = if (searchQuery.isBlank()) {
            routes
        } else {
            routes.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
}

class ActiveRoutesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RoutesUiState())
    val uiState: StateFlow<RoutesUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
    }

    // carga las rutas desde la fuente de datos (los datos de ejemplo en DataSource)
    private fun loadRoutes() {
        viewModelScope.launch {
            val routes = getSampleRoutesSchedule()
            _uiState.update {
                it.copy(
                    routes = routes,
                    isLoading = false
                )
            }
        }
    }


    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
