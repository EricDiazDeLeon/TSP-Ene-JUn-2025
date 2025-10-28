package com.example.mirutadigital.ui.screens.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// enum para manejar la vista de las rutas
enum class RouteView {
    ALL,     // todas las rutas
    ACTIVE // rutas activas
}

// estado de la ui para el contenedor
data class RoutesContainerUiState(
    val currentView: RouteView = RouteView.ALL
)

class RoutesContainerViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutesContainerUiState())
    val uiState: StateFlow<RoutesContainerUiState> = _uiState.asStateFlow()

    init {
        // Sincronizar datos con Firestore al iniciar
        syncDataFromFirestore()
    }

    // Sincroniza las rutas y paradas desde Firestore
    private fun syncDataFromFirestore() {
        viewModelScope.launch {
            try {
                repository.refreshRoutes()
                repository.refreshStops()
            } catch (e: Exception) {
                // Manejar error de sincronización
                // Por ahora solo lo capturamos, podríamos agregar un estado de error si es necesario
            }
        }
    }

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
