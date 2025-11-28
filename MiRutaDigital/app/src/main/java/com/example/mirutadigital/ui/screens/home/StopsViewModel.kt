package com.example.mirutadigital.ui.screens.home

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.MiRutaApplication
import com.example.mirutadigital.data.model.ui.StopWithRoutes
import com.example.mirutadigital.ui.util.SnackbarManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StopsUiState(
    val allStops: List<StopWithRoutes> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val userLocation: Location? = null,
    val isSortedByLocation: Boolean = false
) {
    val filteredStops: List<StopWithRoutes>
        get() = if (searchQuery.isBlank()) {
            allStops
        } else {
            allStops.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
}

class StopsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = (application as MiRutaApplication).repository

    private val _uiState = MutableStateFlow(StopsUiState())
    val uiState: StateFlow<StopsUiState> = _uiState.asStateFlow()

    private var originalStops: List<StopWithRoutes> = emptyList()

    init {
        loadStopsData()
    }

    private fun loadStopsData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isLoading = true) }

                var stops = try {
                    repository.getStopsWithRoutes()
                } catch (e: Exception) {
                    emptyList()
                }

                if (stops.isNotEmpty()) {
                    originalStops = stops
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                allStops = stops,
                                isLoading = false
                            )
                        }
                    }
                }

                if (stops.isEmpty()) {
                    try {
                        repository.synchronizeDatabase()
                        stops = repository.getStopsWithRoutes()
                        originalStops = stops
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            allStops = stops,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                 withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }

        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateUserLocation(location: Location?) {
        _uiState.update { it.copy(userLocation = location) }
    }

    fun sortStopsByProximity(): StopWithRoutes? {
        val currentState = _uiState.value
        val location = currentState.userLocation

        if (location == null) {
            viewModelScope.launch {
                SnackbarManager.showMessage("No se puede ordenar: ubicación no disponible.")
            }
            return null
        }

        val sortedStops = currentState.allStops.sortedBy { stop ->
            val stopLocation = Location("").apply {
                latitude = stop.latitude
                longitude = stop.longitude
            }
            location.distanceTo(stopLocation)
        }

        _uiState.update {
            it.copy(allStops = sortedStops, isSortedByLocation = true)
        }

        return sortedStops.firstOrNull()
    }

    fun resetStopsOrder() {
        _uiState.update {
            it.copy(allStops = originalStops, isSortedByLocation = false)
        }
    }
}
