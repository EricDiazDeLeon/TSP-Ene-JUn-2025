package com.example.mirutadigital.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.repository.RouteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RouteViewModel(private val repository: RouteRepository) : ViewModel() {
    
    // Estado para la UI
    private val _uiState = MutableStateFlow<RouteUiState>(RouteUiState.Loading)
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()
    
    // Obtener todas las rutas
    fun getAllRoutes() {
        viewModelScope.launch {
            repository.routes.collect { routes ->
                _uiState.value = RouteUiState.Success(routes)
            }
        }
    }
    
    // Obtener rutas favoritas
    fun getFavoriteRoutes() {
        viewModelScope.launch {
            repository.favoriteRoutes.collect { favoriteRoutes ->
                _uiState.value = RouteUiState.Success(favoriteRoutes)
            }
        }
    }
    
    // Cambiar estado de favorito
    fun toggleFavorite(routeId: String) {
        viewModelScope.launch {
            val route = repository.getRouteById(routeId)
            route?.let {
                val newFavoriteStatus = !it.isFavorite
                val success = repository.setFavoriteRoute(routeId, newFavoriteStatus)
                if (!success) {
                    _uiState.value = RouteUiState.Error("No se pudo guardar la ruta como favorita")
                }
            }
        }
    }
}

// Estados de UI para la pantalla de rutas
sealed class RouteUiState {
    object Loading : RouteUiState()
    data class Success(val routes: List<Route>) : RouteUiState()
    data class Error(val message: String) : RouteUiState()
}