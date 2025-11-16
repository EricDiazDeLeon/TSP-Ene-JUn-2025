package com.example.mirutadigital.ui.screens.destinationSearch

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.MiRutaApplication
import com.example.mirutadigital.R
import com.example.mirutadigital.data.model.ui.StopWithRoutes
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SearchResult {
    data class Success(
        val closestStop: StopWithRoutes,
        val destination: LatLng,
        val destinationName: String
    ) : SearchResult()

    object NoResult : SearchResult()
    object Error : SearchResult()
}

data class DestinationSearchUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val searchResult: SearchResult? = null
)

class DestinationSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiRutaApplication).repository
    private val apiKey = application.getString(R.string.maps_api_key)

    private val _uiState = MutableStateFlow(DestinationSearchUiState())
    val uiState: StateFlow<DestinationSearchUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchDestination() {
        if (_uiState.value.searchQuery.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, searchResult = null) }

            val geocodingResult =
                repository.getCoordinatesForAddress(_uiState.value.searchQuery, apiKey)

            if (geocodingResult == null) {
                _uiState.update { it.copy(isLoading = false, searchResult = SearchResult.Error) }
                return@launch
            }

            val location = geocodingResult.geometry.location
            val targetLatLng = LatLng(location.lat, location.lng)
            val allStops = repository.getStopsWithRoutes()
            val closestStop = findClosestStop(targetLatLng, allStops)

            val result = if (closestStop != null) {
                SearchResult.Success(
                    closestStop,
                    targetLatLng,
                    geocodingResult.formattedAddress ?: _uiState.value.searchQuery
                )
            } else {
                SearchResult.NoResult
            }

            _uiState.update { it.copy(isLoading = false, searchResult = result) }
        }
    }

    private fun findClosestStop(target: LatLng, stops: List<StopWithRoutes>): StopWithRoutes? {
        val targetLocation = Location("").apply {
            latitude = target.latitude
            longitude = target.longitude
        }

        return stops.map { stop ->
            val stopLocation = Location("").apply {
                latitude = stop.latitude
                longitude = stop.longitude
            }
            val distance = targetLocation.distanceTo(stopLocation)
            Pair(stop, distance)
        }.minByOrNull { it.second }?.let { (stop, distance) ->
            stop.copy(distance = "A ${distance.toInt()} metros del destino")
        }
    }
}
