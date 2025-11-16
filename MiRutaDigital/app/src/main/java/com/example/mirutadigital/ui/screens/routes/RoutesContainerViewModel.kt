package com.example.mirutadigital.ui.screens.routes

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// enum para manejar la vista de las rutas
enum class RouteView {
    ALL,     // todas las rutas
    ACTIVE // rutas activas
}

// estado de la ui para el contenedor
data class RoutesContainerUiState(
    val currentView: RouteView = RouteView.ALL
)

class RoutesContainerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RoutesContainerUiState())
    val uiState: StateFlow<RoutesContainerUiState> = _uiState.asStateFlow()

    // para alternar la vista
    fun toggleView() {
        _uiState.update { currentState ->
            val newView = if (currentState.currentView == RouteView.ACTIVE) {

                RouteView.ALL
            } else {
                RouteView.ACTIVE
            }
            currentState.copy(currentView = newView)
        }
    }
}
