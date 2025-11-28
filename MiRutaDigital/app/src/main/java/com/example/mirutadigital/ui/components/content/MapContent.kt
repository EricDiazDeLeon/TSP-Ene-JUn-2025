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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.mirutadigital.ui.util.MapIcons
import com.example.mirutadigital.viewModel.MapDisplayMode
import com.example.mirutadigital.viewModel.MapStateViewModel
import com.example.mirutadigital.viewModel.SelectionMode
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapContent(
    mapStateViewModel: MapStateViewModel
) {
    val context = LocalContext.current
    val mapState by mapStateViewModel.mapState.collectAsState()
    val selectedStopId = mapState.selectedStopId
    val displayMode = mapState.displayMode
    val initialLocation = mapStateViewModel.initialLocation

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
                is MapDisplayMode.RouteDetail, is MapDisplayMode.SharedVehicles -> {
                    displayMode.bounds?.let { bounds ->
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(bounds, 100),
                            1000
                        )
                    }
                }

                is MapDisplayMode.AllStops -> {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(initialLocation, 14f),
                        1000
                    )
                }
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true),
        onMapClick = { 
            // Si estamos seleccionando, no deseleccionamos parada al hacer clic en el mapa
            if (mapState.selectionMode == SelectionMode.NONE) {
                 mapStateViewModel.setSelectedStop(null) 
            }
        },
        onMapLongClick = { latLng ->
            // Solo permite seleccionar si estamos en modo seleccion
            if (mapState.selectionMode != SelectionMode.NONE) {
                mapStateViewModel.setTemporaryPoint(latLng)
            }
        },
        uiSettings = MapUiSettings(myLocationButtonEnabled = true),
        onMapLoaded = { isMapLoaded = true }
    ) {
        // Renderizado de puntos temporales y confirmados para el plan de viaje
        if (mapState.temporaryPoint != null) {
            Marker(
                state = MarkerState(position = mapState.temporaryPoint!!),
                title = "Punto Seleccionado",
                snippet = "Toca Confirmar para usar este punto",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
            )
        }

        if (mapState.confirmedOrigin != null) {
             Marker(
                state = MarkerState(position = mapState.confirmedOrigin!!),
                title = "Origen",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }

        if (mapState.confirmedDestination != null) {
             Marker(
                state = MarkerState(position = mapState.confirmedDestination!!),
                title = "Destino",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }


        when (displayMode) {
            is MapDisplayMode.AllStops -> {
                val stopsToShow = if (displayMode.focusedStopId != null) {
                    mapState.allStops.filter { it.id == displayMode.focusedStopId }
                } else {
                    mapState.allStops
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
                            if (mapState.selectionMode != SelectionMode.NONE) {
                                // Si estamos seleccionando, usar la ubicación de la parada
                                mapStateViewModel.setTemporaryPoint(LatLng(stop.latitude, stop.longitude))
                            } else {
                                mapStateViewModel.setSelectedStop(stop.id) 
                            }
                        },
                        onInfoWindowClick = { mapStateViewModel.setSelectedStop(null) }
                    )
                }
            }

            is MapDisplayMode.RouteDetail -> {
                Polyline(
                    points = displayMode.outboundPolyline,
                    color = Color.Blue,
                    width = 10f,
                    startCap = RoundCap(),
                    endCap = RoundCap()
                )
                Polyline(
                    points = displayMode.inboundPolyline,
                    color = Color.Red,
                    width = 10f,
                    startCap = RoundCap(),
                    endCap = RoundCap()
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
                             if (mapState.selectionMode != SelectionMode.NONE) {
                                mapStateViewModel.setTemporaryPoint(stop.coordinates)
                            } else {
                                mapStateViewModel.setSelectedStop(stop.id) 
                            }
                        },
                        onInfoWindowClick = { mapStateViewModel.setSelectedStop(null) }
                    )
                }
            }

            is MapDisplayMode.SharedVehicles -> {
                Polyline(
                    points = displayMode.outboundPolyline,
                    color = Color.Blue.copy(alpha = 0.7f),
                    width = 9f,
                    startCap = RoundCap(),
                    endCap = RoundCap()
                )
                Polyline(
                    points = displayMode.inboundPolyline,
                    color = Color.Red.copy(alpha = 0.7f),
                    width = 9f,
                    startCap = RoundCap(),
                    endCap = RoundCap()
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
                             if (mapState.selectionMode != SelectionMode.NONE) {
                                mapStateViewModel.setTemporaryPoint(stop.coordinates)
                            } else {
                                mapStateViewModel.setSelectedStop(stop.id) 
                            }
                        },
                        onInfoWindowClick = { mapStateViewModel.setSelectedStop(null) }
                    )
                }

                displayMode.bus.forEach { bus ->
                    val position =
                        LatLng(bus.lastLocation.latitude, bus.lastLocation.longitude)
                    val journeyColor =
                        if (bus.journeyType == "outbound") Color.Blue else Color.Red

                    Marker(
                        state = MarkerState(position = position),
                        title = "Bus Compartido",
                        snippet = "Trayecto: ${bus.journeyType}",
                        icon = MapIcons.getBusIcon(context.applicationContext, journeyColor),
                        zIndex = 10f,
                        onClick = { false }
                    )
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
    onMarkerClick: () -> Unit,
    onInfoWindowClick: () -> Unit
) {
    val context = LocalContext.current

    val markerState = remember(stopId) {
        MarkerState(position = coordinates).also {
            markerStates[stopId] = it
        }
    }

    Marker(
        state = markerState,
        title = stopName,
        icon = if (isSelected) {
            MapIcons.getActiveStopIcon(context.applicationContext)
        } else {
            MapIcons.getInactiveStopIcon(context.applicationContext)
        },
        anchor = Offset(0.5f, 0.5f),
        zIndex = if (isSelected) 2f else 1f,
        onClick = {
            onMarkerClick()
            false // para la info window
        },
        onInfoWindowClick = { onInfoWindowClick() }
    )
}