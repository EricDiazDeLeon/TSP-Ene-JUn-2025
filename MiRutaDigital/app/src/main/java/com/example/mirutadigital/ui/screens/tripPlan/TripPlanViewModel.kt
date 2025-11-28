package com.example.mirutadigital.ui.screens.tripPlan

import android.app.Application
import android.location.Location
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.MiRutaApplication
import com.example.mirutadigital.data.model.ui.base.Stop
import com.example.mirutadigital.ui.util.TripPathfinder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class TripStep {
    abstract val durationMinutes: Int
    abstract val description: String
}

data class WalkStep(
    override val durationMinutes: Int,
    val distanceMeters: Int,
    override val description: String = "Caminar",
    val instructions: String // "Camina hacia Av. Hidalgo"
) : TripStep()

data class BusStep(
    override val durationMinutes: Int,
    val routeName: String,
    val routeColor: Color,
    val boardStopName: String,
    val alightStopName: String,
    val stopsCount: Int,
    val direction: String?,
    override val description: String = "Viaje en autobús"
) : TripStep()

data class TripItinerary(
    val totalDurationMinutes: Int,
    val startTime: String,
    val endTime: String,
    val steps: List<TripStep>
)

sealed class TripScreenState {
    object Input : TripScreenState()
    object Loading : TripScreenState()
    data class Results(val itinerary: TripItinerary) : TripScreenState()
    object Error : TripScreenState()
}

data class PlanUiState(
    val userLocation: Location? = null,
    val screenState: TripScreenState = TripScreenState.Input
)

class TripPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState: StateFlow<PlanUiState> = _uiState.asStateFlow()
    
    private val repository = (application as MiRutaApplication).repository
    private val pathfinder = TripPathfinder()
    private var isGraphBuilt = false

    fun updateUserLocation(location: Location?) {
        _uiState.update { it.copy(userLocation = location) }
    }

    fun resetToInput() {
        _uiState.update { it.copy(screenState = TripScreenState.Input) }
    }

    fun findRoute(origin: LatLng?, destination: LatLng?) {
        if (origin == null || destination == null) return

        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(screenState = TripScreenState.Loading) }

            if (!isGraphBuilt) {
                val stopsWithRoutes = try { repository.getStopsWithRoutes() } catch (e: Exception) { emptyList() }
                val routes = try { repository.getGeneralRoutesInfo() } catch (e: Exception) { emptyList() }
                
                val stops = stopsWithRoutes.map { 
                    Stop(
                        id = it.id,
                        name = it.name,
                        coordinates = LatLng(it.latitude, it.longitude)
                    ) 
                }

                if (stops.isNotEmpty() && routes.isNotEmpty()) {
                    pathfinder.buildGraph(stops, routes)
                    isGraphBuilt = true
                } else {
                    _uiState.update { it.copy(screenState = TripScreenState.Error) }
                    return@launch
                }
            }

            val itinerary = pathfinder.findPath(origin, destination)

            withContext(Dispatchers.Main) {
                if (itinerary != null) {
                    _uiState.update { it.copy(screenState = TripScreenState.Results(itinerary)) }
                } else {
                    _uiState.update { it.copy(screenState = TripScreenState.Error) } 
                }
            }
        }
    }
}
