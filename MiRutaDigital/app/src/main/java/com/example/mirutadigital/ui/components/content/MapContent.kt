package com.example.mirutadigital.ui.components.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.mirutadigital.ui.util.ActiveStopIcon
import com.example.mirutadigital.ui.util.InactiveStopIcon
import com.example.mirutadigital.ui.util.SharedBusIcon
import com.example.mirutadigital.viewModel.MapDisplayMode
import com.example.mirutadigital.viewModel.MapStateViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Composable principal del mapa de Google y sus elementos
 *
 * Se encarga de observar el estado del mapa desde [mapStateViewModel] y mostrar
 * diferentes elementos (marcadores de todas las paradas, o una ruta especifica con su polilinea)
 * en función del modo [MapDisplayMode] actual
 *
 * Gestiona la posición de la camara, la eleccion de marcadores y las animaciones
 *
 * @param mapStateViewModel El ViewModel que tiene y gestiona el estado del mapa
 */
@Composable
fun MapContent(
    mapStateViewModel: MapStateViewModel
) {
    // observa el estado del mapa
    val mapState by mapStateViewModel.mapState.collectAsState()
    val selectedStopId = mapState.selectedStopId
    val displayMode = mapState.displayMode

    val initialLocation = mapStateViewModel.initialLocation
    //val markers = mapState.allStops.map { it. }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 14f)
    }

    val markerStates = remember { mutableMapOf<String, MarkerState>() }

    var isMapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedStopId, markerStates, displayMode, isMapLoaded) {
        if (!isMapLoaded) return@LaunchedEffect

        if (selectedStopId != null) {
            val stopToSelect = when (displayMode) {
                is MapDisplayMode.AllStops -> mapState.allStops.find { it.id == selectedStopId }
                    ?.let { LatLng(it.latitude, it.longitude) }
                is MapDisplayMode.RouteDetail ->
                    displayMode.routeStops.find { it.id == selectedStopId }?.coordinates
                is MapDisplayMode.SharedVehicles ->
                    displayMode.routeStops.find { it.id == selectedStopId }?.coordinates
            }

            stopToSelect?.let { latLng ->
                cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng))
                markerStates[selectedStopId]?.showInfoWindow()
            }
        } else {
            markerStates.values.forEach { it.hideInfoWindow() }

            when (displayMode) {
                is MapDisplayMode.RouteDetail -> {
                    if (displayMode.bounds.center.latitude != 0.0) {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(displayMode.bounds, 100),
                            1000
                        )
                    }
                }
                is MapDisplayMode.SharedVehicles -> {
                    if (displayMode.bounds.center.latitude != 0.0) {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(displayMode.bounds, 100),
                            1000
                        )
                    }
                }
                is MapDisplayMode.AllStops -> {
                }
            }
        }
    }

    // composable de GoogleMap
    GoogleMap(
        modifier = Modifier
            .fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true
            //mapType = MapType.SATELLITE
        ),
        onMapClick = { mapStateViewModel.setSelectedStop(null) },
        uiSettings = com.google.maps.android.compose.MapUiSettings(
            myLocationButtonEnabled = true
        ),
        onMapLoaded = { isMapLoaded = true }
    ) {
        when (displayMode) {
            is MapDisplayMode.AllStops -> {
                val stopsToShow = if (displayMode.focusedStopId != null) {
                    mapState.allStops.filter { it.id == displayMode.focusedStopId } // en foco
                } else {
                    mapState.allStops // todas
                }

                stopsToShow.forEach { stop ->
                    val isSelected = stop.id == selectedStopId
                    StopMarker(
                        stopId = stop.id,
                        stopName = stop.name,
                        isSelected = isSelected,
                        coordinates = LatLng(stop.latitude, stop.longitude),
                        markerStates = markerStates,
                        onMarkerClick = {
                            mapStateViewModel.setSelectedStop(stop.id)
                            false
                        },
                        onInfoWindowClick = { mapStateViewModel.setSelectedStop(null) }
                    )
                }
            }

            is MapDisplayMode.RouteDetail -> {
                Polyline(
                    points = displayMode.outboundPolyline,
                    color = Color.Blue, //.copy(alpha = 0.7f),
                    width = 10f,
                    endCap = RoundCap(),
                    startCap = RoundCap()
                )
                Polyline(
                    points = displayMode.inboundPolyline,
                    color = Color.Red, //.copy(alpha = 0.7f),
                    width = 10f,
                    endCap = RoundCap(),
                    startCap = RoundCap()
                )

                displayMode.routeStops.forEach { stop ->
                    val isSelected = stop.id == selectedStopId
                    StopMarker(
                        stopId = stop.id,
                        stopName = stop.name,
                        isSelected = isSelected,
                        coordinates = stop.coordinates,
                        markerStates = markerStates,
                        onMarkerClick = {
                            mapStateViewModel.setSelectedStop(stop.id)
                            false
                        },
                        onInfoWindowClick = { mapStateViewModel.setSelectedStop(null) }
                    )
                }
            }

            is MapDisplayMode.SharedVehicles -> {

                Polyline(
                    points = displayMode.outboundPolyline,
                    color = Color.Blue.copy(alpha = 0.7f),
                    width = 10f,
                    endCap = RoundCap(),
                    startCap = RoundCap()
                )
                Polyline(
                    points = displayMode.inboundPolyline,
                    color = Color.Red.copy(alpha = 0.7f),
                    width = 10f,
                    endCap = RoundCap(),
                    startCap = RoundCap()
                )

                displayMode.routeStops.forEach { stop ->
                    val isSelected = stop.id == selectedStopId // Usamos el selectedStopId del estado global
                    StopMarker(
                        stopId = stop.id,
                        stopName = stop.name,
                        isSelected = isSelected,
                        coordinates = stop.coordinates,
                        markerStates = markerStates,
                        onMarkerClick = {
                            mapStateViewModel.setSelectedStop(stop.id)
                            false
                        },
                        onInfoWindowClick = { mapStateViewModel.setSelectedStop(null) }
                    )
                }

                displayMode.vehicles.forEach { vehicle ->
                    val position = LatLng(vehicle.lastLocation.latitude, vehicle.lastLocation.longitude)
                    val journeyColor = if (vehicle.journeyType == "outbound") Color.Blue else Color.Red

                    MarkerComposable(
                        state = MarkerState(position = position),
                        title = "Vehículo Compartido",
                        snippet = "Trayecto: ${vehicle.journeyType}",
                        zIndex = 10f, // Prioridad alta para que se vea sobre las paradas
                        onClick = { false }
                    ) {
                        SharedBusIcon(color = journeyColor)
                    }
                }
            }
        }
    }
}

@Composable
fun StopMarker(
    stopId: String,
    stopName: String,
    isSelected: Boolean,
    coordinates: LatLng,
    markerStates: MutableMap<String, MarkerState>,
    onMarkerClick: () -> Boolean,
    onInfoWindowClick: () -> Unit
) {
    val markerState = remember(stopId) {
        MarkerState(position = coordinates).also {
            markerStates[stopId] = it
        }
    }

    MarkerComposable(
        keys = arrayOf(isSelected),
        state = markerState,
        title = stopName,
        onClick = { onMarkerClick() },
        onInfoWindowClick = { onInfoWindowClick() }
    ) {
        if (isSelected) {
            ActiveStopIcon()
        } else {
            InactiveStopIcon()
        }
    }
}
