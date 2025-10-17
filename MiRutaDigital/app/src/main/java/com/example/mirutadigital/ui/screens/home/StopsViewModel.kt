package com.example.mirutadigital.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.testsUi.StopWithRoutes
import com.example.mirutadigital.data.testsUi.getSampleStopsWithRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * define el estado de la ui para StopsSheetContent
 * encapsula propiedades que el composable necesita para funcionar
 * que antes se manejaban dentro de el
 */
data class StopsUiState(
    val allStops: List<StopWithRoutes> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
) {
    // propiedad que se calcula y que devuelve la lista de paradas filtradas
    val filteredStops: List<StopWithRoutes>
        get() = if (searchQuery.isBlank()) {
            allStops
        } else {
            allStops.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
}

class StopsViewModel : ViewModel() {

    // estado privado que contiene toda la información de la ui
    private val _uiState = MutableStateFlow(StopsUiState())
    // estado publico de solo lectura para que la ui lo observe
    val uiState: StateFlow<StopsUiState> = _uiState.asStateFlow()

    init {
        loadStopsData()
    }

    // carga la lista inicial de paradas
    private fun loadStopsData() {
        viewModelScope.launch {
            val stops = getSampleStopsWithRoutes()
            _uiState.update {
                it.copy(
                    allStops = stops,
                    isLoading = false
                )
            }
        }
    }

    // funcion publica que llama la ui cuando se escribe en la barra de busqueda
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query,)
        }
    }
}
