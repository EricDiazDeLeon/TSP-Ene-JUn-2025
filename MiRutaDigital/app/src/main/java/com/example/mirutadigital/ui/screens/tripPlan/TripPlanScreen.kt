package com.example.mirutadigital.ui.screens.tripPlan

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mirutadigital.viewModel.LocationViewModel
import com.example.mirutadigital.viewModel.MapStateViewModel
import com.example.mirutadigital.viewModel.SelectionMode
import com.google.android.gms.maps.model.LatLng

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TripPlanScreen(
    navController: NavController,
    viewModel: TripPlanViewModel = viewModel(),
    mapStateViewModel: MapStateViewModel,
    locationViewModel: LocationViewModel,
    isSheetExpanded: Boolean,
    onExpandSheet: () -> Unit
) {
    val userLocation by locationViewModel.location.collectAsState()
    val mapState by mapStateViewModel.mapState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userLocation) {
        viewModel.updateUserLocation(userLocation)
    }

    DisposableEffect(Unit) {
        onDispose {
            mapStateViewModel.clearPlanPoints()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
    ) {
        when (uiState.screenState) {
            is TripScreenState.Input -> {
                InputStateContent(
                    mapState = mapState,
                    mapStateViewModel = mapStateViewModel,
                    onExpandSheet = onExpandSheet,
                    onFindRoute = {
                        viewModel.findRoute(
                            mapState.confirmedOrigin,
                            mapState.confirmedDestination
                        )
                    }
                )
            }
            is TripScreenState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondaryFixedVariant)
                }
            }
            is TripScreenState.Results -> {
                val itinerary = (uiState.screenState as TripScreenState.Results).itinerary
                ResultsStateContent(
                    itinerary = itinerary,
                    onBackToInput = { viewModel.resetToInput() }
                )
            }
            is TripScreenState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                        .padding(horizontal = 16.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No se encontró una ruta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Intenta mover los puntos de origen/destino más cerca de avenidas principales o paradas.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { viewModel.resetToInput() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Intentar de nuevo")
                    }
                }
            }
        }
    }
}

@Composable
fun InputStateContent(
    mapState: com.example.mirutadigital.viewModel.MapState,
    mapStateViewModel: MapStateViewModel,
    onExpandSheet: () -> Unit,
    onFindRoute: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onExpandSheet,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(vertical = 4.dp, horizontal = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSecondaryFixedVariant
            )
        ) {
            Icon(Icons.Default.Map, contentDescription = "Planificar viaje")
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Planear viaje")
        }

        LocationSelector(
            label = "Origen",
            icon = Icons.Default.LocationOn,
            iconTint = Color(0xFF4CAF50), // Green-ish
            isSelectedMode = mapState.selectionMode == SelectionMode.ORIGIN,
            location = mapState.confirmedOrigin,
            previewLocation = if (mapState.selectionMode == SelectionMode.ORIGIN) mapState.temporaryPoint else null,
            onSelectClick = { mapStateViewModel.setSelectionMode(SelectionMode.ORIGIN) }
        )

        LocationSelector(
            label = "Destino",
            icon = Icons.Default.LocationOn,
            iconTint = Color(0xFFF44336), // Red-ish
            isSelectedMode = mapState.selectionMode == SelectionMode.DESTINATION,
            location = mapState.confirmedDestination,
            previewLocation = if (mapState.selectionMode == SelectionMode.DESTINATION) mapState.temporaryPoint else null,
            onSelectClick = { mapStateViewModel.setSelectionMode(SelectionMode.DESTINATION) }
        )

        if (mapState.selectionMode != SelectionMode.NONE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.inverseSurface,
                        shape = RoundedCornerShape(8.dp)
                        )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (mapState.temporaryPoint == null)
                        "Mantén presionado el mapa o selecciona una parada"
                    else
                        "Ubicación marcada. Confirma para guardar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }

        val isRouteReady = mapState.confirmedOrigin != null &&
                mapState.confirmedDestination != null &&
                mapState.selectionMode == SelectionMode.NONE

        if (isRouteReady) {
            Button(
                onClick = onFindRoute,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                    contentColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Icon(Icons.Default.Directions, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Encontrar ruta")
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { mapStateViewModel.confirmSelection() },
                    enabled = mapState.temporaryPoint != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                        contentColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContainerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Confirmar")
                }

                Button(
                    onClick = { mapStateViewModel.cancelSelection() },
                    enabled = mapState.selectionMode != SelectionMode.NONE,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onSecondaryFixedVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
fun ResultsStateContent(
    itinerary: TripItinerary,
    onBackToInput: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackToInput,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${itinerary.totalDurationMinutes} min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${itinerary.startTime} - ${itinerary.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryFixedVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(itinerary.steps) { step ->
                TimelineItem(step = step)
            }
            
             item {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                     Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PinDrop,
                            contentDescription = "Destino",
                            tint = Color(0xFFF44336), // Red-ish
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Llegada a tu destino",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineItem(step: TripStep) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            when (step) {
                is WalkStep -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = "Caminar",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                     Column(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .padding(vertical = 2.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(5) {
                             Box(
                                modifier = Modifier
                                    .size(2.dp)
                                    .background(Color.Gray, CircleShape)
                            )
                             Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
                is BusStep -> {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = "Subir",
                        tint = step.routeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .weight(1f)
                            .background(step.routeColor)
                    )
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = "Bajar",
                        tint = step.routeColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 24.dp)
        ) {
            when (step) {
                is WalkStep -> {
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = step.instructions,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${step.durationMinutes} min (${step.distanceMeters} m)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                is BusStep -> {
                    Text(
                        text = "Sube en ${step.boardStopName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Tarjeta de Ruta
                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = null,
                            tint = step.routeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = step.routeName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = step.routeColor
                            )
                            Text(
                                text = "${step.stopsCount} paradas • ${step.durationMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Text(
                        text = "Baja en ${step.alightStopName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LocationSelector(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    isSelectedMode: Boolean,
    location: LatLng?,
    previewLocation: LatLng?,
    onSelectClick: () -> Unit
) {
    val containerColor = if (isSelectedMode) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }

    val contentColor = if (isSelectedMode) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val border = if (!isSelectedMode) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
    } else {
        null
    }

    val secondaryText = when {
        previewLocation != null -> "Punto: ${previewLocation.latitude.toString().take(7)}, ${previewLocation.longitude.toString().take(7)}"
        location != null -> "Ubicación: ${location.latitude.toString().take(7)}, ${location.longitude.toString().take(7)}"
        isSelectedMode -> "Toca el mapa..."
        else -> "Seleccionar ubicación"
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .then(if (border != null) Modifier.border(border, RoundedCornerShape(12.dp)) else Modifier)
                .clickable { onSelectClick() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }

            if (isSelectedMode) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Seleccionando",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            } else if (location != null) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirmado",
                    tint = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
