package com.example.mirutadigital.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.model.Stop
import com.example.mirutadigital.data.repository.AppRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import com.google.maps.android.PolyUtil

data class RouteUi(
    val id: String,
    val name: String,
    val operatingHours: String
)

data class StopUi(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val routes: List<RouteUi>
)

data class HomeUiState(
    val stops: List<StopUi> = emptyList(),
    val routes: List<Route> = emptyList(),
    val selectedRoute: Route? = null,
    val detailedPolyline: List<LatLng> = emptyList(), // <-- AÑADE ESTO
    val isRouteLoading: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredStops: List<StopUi>
        get() = if (searchQuery.isBlank()) stops
        else stops.filter { it.name.contains(searchQuery, ignoreCase = true) }
}

class HomeScreenViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStopsAndRoutes()
        refreshFromFirebase()
    }

    private fun loadStopsAndRoutes() {
        viewModelScope.launch {
            combine(
                repository.getStops(),
                repository.getRoutes()
            ) { stops, routes ->
                // --- PASO 1: LA OPTIMIZACIÓN CLAVE ---
                // Convertimos la lista de rutas en un mapa para búsquedas instantáneas (O(1)).
                // La clave del mapa será el ID de la ruta.
                val routesMap = routes.associateBy { it.id }

                // --- PASO 2: EL MAPEO EFICIENTE ---
                // Ahora, mapear las paradas es muchísimo más rápido.
                val mappedStops = stops.map { stop ->
                    // Por cada ID de ruta asociada a la parada, lo buscamos directamente en el mapa.
                    // No más bucles anidados.
                    val associatedRoutes = stop.associatedRouteIds.mapNotNull { routeId ->
                        routesMap[routeId]
                    }

                    StopUi(
                        id = stop.id,
                        name = stop.name,
                        latitude = stop.location.latitude,
                        longitude = stop.location.longitude,
                        routes = associatedRoutes.map {
                            RouteUi(
                                id = it.id,
                                name = it.name,
                                operatingHours = it.operatingHours
                            )
                        }
                    )
                }
                // Empaquetamos los dos resultados para el 'collect'
                Pair(mappedStops, routes)

            }
                // --- PASO 3: MOVER TODO EL TRABAJO A UN HILO DE FONDO ---
                // flowOn le dice a todo el 'combine' y 'map' de arriba que se ejecute
                // en un hilo optimizado para cálculos (Dispatchers.Default).
                .flowOn(Dispatchers.Default)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { (mappedStops, routes) ->
                    // Finalmente, el hilo principal solo recibe los datos ya listos y actualiza la UI.
                    _uiState.update { currentState ->
                        currentState.copy(
                            stops = mappedStops,
                            routes = routes,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun mapStopsToUi(stops: List<Stop>, routes: List<Route>): List<StopUi> {
        return stops.map { stop ->
            val associatedRoutes = routes.filter { route -> stop.associatedRouteIds.contains(route.id) }
            StopUi(
                id = stop.id,
                name = stop.name,
                latitude = stop.location.latitude,
                longitude = stop.location.longitude,
                routes = associatedRoutes.map {
                    RouteUi(
                        id = it.id,
                        name = it.name,
                        operatingHours = it.operatingHours
                    )
                }
            )
        }
    }

    private fun refreshFromFirebase() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                repository.refreshRoutes()
                repository.refreshStops()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onRouteSelected(route: Route, apiKey: String) {
        // Ignora si la ruta no tiene al menos un origen y un destino
        if (route.polylinePoints.size < 2) return

        viewModelScope.launch(Dispatchers.IO) { // Ejecutar en un hilo de fondo
            _uiState.update { it.copy(isRouteLoading = true, detailedPolyline = emptyList()) }

            try {
                // Configura el contexto de la API
                val geoApiContext = GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build()

                // Define el origen, destino y los puntos intermedios
                val origin = route.polylinePoints.first()
                val destination = route.polylinePoints.last()
                val waypoints = route.polylinePoints.subList(1, route.polylinePoints.size - 1)

                // Llama a la API de Direcciones
                val directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .waypoints(*waypoints.map { com.google.maps.model.LatLng(it.latitude, it.longitude) }.toTypedArray())
                    .mode(TravelMode.DRIVING)
                    .await()

                // La API devuelve varias rutas posibles, normalmente usamos la primera
                val path = directionsResult.routes.firstOrNull()?.overviewPolyline?.encodedPath

                if (path != null) {
                    // Decodifica el string de la polilínea en una lista de LatLng
                    val decodedPath = PolyUtil.decode(path)
                    _uiState.update {
                        it.copy(
                            selectedRoute = route,
                            detailedPolyline = decodedPath, // <-- Guarda la ruta detallada
                            isRouteLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isRouteLoading = false) }
                }

            } catch (e: Exception) {
                // Maneja el error (ej. mostrar un Toast)
                e.printStackTrace()
                _uiState.update { it.copy(isRouteLoading = false) }
            }
        }
    }

    fun clearSelectedRoute() {
        _uiState.update { it.copy(selectedRoute = null, detailedPolyline = emptyList()) }
    }
}
