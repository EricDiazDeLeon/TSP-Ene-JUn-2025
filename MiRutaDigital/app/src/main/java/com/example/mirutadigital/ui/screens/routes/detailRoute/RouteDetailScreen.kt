package com.example.mirutadigital.ui.screens.routes.detailRoute

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mirutadigital.data.model.ui.JourneyDetailInfo
import com.example.mirutadigital.viewModel.MapDisplayMode
import com.example.mirutadigital.viewModel.MapStateViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RouteDetailScreen(
    routeId: String,
    navController: NavController,
    mapStateViewModel: MapStateViewModel,
    viewModel: RouteDetailViewModel = viewModel(),
    onExpandSheet: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val mapState by mapStateViewModel.mapState.collectAsState()
    val route = uiState.route
    val etaResult = uiState.etaResult

    val mapMode = mapState.displayMode
    val isShowingSharedVehicles = mapMode is MapDisplayMode.SharedVehicles && mapMode.routeId == routeId

    LaunchedEffect(routeId) {
        viewModel.loadRouteById(routeId)
    }

    LaunchedEffect(mapState.selectedStopId, uiState.routeMap) {
        if (uiState.routeMap != null) {
            viewModel.updateSelectedStopInfo(mapState.selectedStopId)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
    ) {
        stickyHeader {
            RouteDetailHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                isFavorite = uiState.isFavorite,
                onToggleFavorite = { viewModel.toggleFavorite(routeId) },
                onExpandSheet = onExpandSheet
            )
        }

        item {
            val nullInfoJourney = JourneyDetailInfo("N/A", "N/A", "N/A", "N/A")
            // informacion principal de la ruta
            RouteInfoCard(
                etaResult = etaResult?: "",
                onCalculateEtaClick = {
                    viewModel.calculateEta()
                },
                routeName = route?.name ?: "N/A",
                colors = route?.colors ?: "N/A",
                windshieldLabel = route?.windshieldLabel ?: "N/A",
                price = route?.price ?: 0.0,
                outbound = route?.outboundJourney ?: nullInfoJourney,
                inbound = route?.inboundJourney ?: nullInfoJourney,
                ratingAverage = uiState.ratingAverage,
                ratingCount = uiState.ratingCount,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (isShowingSharedVehicles) {
                        mapStateViewModel.showRouteDetailById(routeId)
                    } else {
                        mapStateViewModel.showSharedVehiclesForRoute(routeId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                val icon = if (isShowingSharedVehicles) Icons.Default.VisibilityOff else Icons.Default.Groups
                val text = if (isShowingSharedVehicles) "Ocultar Vehículos" else "Mostrar Vehículos Compartidos"

                Icon(icon, contentDescription = text)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text)
            }
            if (isShowingSharedVehicles) {
                Button(
                    onClick = {
                        mapStateViewModel.showSharedVehiclesForRoute(routeId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar Ubicaciones")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizar Ubicaciones")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.openRatingDialog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Star, contentDescription = "Calificar Ruta")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calificar esta Ruta")
            }
            if (uiState.ratingMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.ratingMessage ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }

    if (uiState.showRatingDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeRatingDialog() },
            confirmButton = {
                TextButton(onClick = { viewModel.submitRating(routeId) }, enabled = uiState.ratingStars >= 1) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeRatingDialog() }) { Text("Cancelar") }
            },
            title = { Text("Calificar Ruta") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        (1..5).forEach { i ->
                            IconButton(onClick = { viewModel.setRatingStars(i) }) {
                                Icon(
                                    imageVector = if (i <= uiState.ratingStars) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$i estrellas",
                                    tint = if (i <= uiState.ratingStars) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (uiState.ratingMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.ratingMessage ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.ratingComment,
                        onValueChange = { viewModel.setRatingComment(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Comentario (opcional)") }
                    )
                }
            }
        )
    }
}

@Composable
fun RouteDetailHeader(
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onExpandSheet: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onExpandSheet,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSecondaryFixedVariant
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Map, contentDescription = "Información De La Ruta")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Información De La Ruta",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSecondaryFixedVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun RouteInfoCard(
    etaResult: String,
    onCalculateEtaClick: () -> Unit,
    routeName: String,
    colors: String,
    windshieldLabel: String,
    price: Double,
    outbound: JourneyDetailInfo,
    inbound: JourneyDetailInfo,
    ratingAverage: Double,
    ratingCount: Int,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ruta " + routeName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
                Text(
                    text = "Precio: $$price",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = String.format("%.1f (%d)", ratingAverage, ratingCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Colores:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                    modifier = Modifier.width(120.dp)
                )
                Text(
                    text = colors,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Encabezado:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                    modifier = Modifier.width(120.dp)
                )
                Text(
                    text = "\"$windshieldLabel\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "Horario:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                        modifier = Modifier.width(120.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(top = 10.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryFixedVariant
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${outbound.startStopName} - ${outbound.endStopName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                    )
                    Text(
                        text = "${outbound.firstDeparture} - ${outbound.lastDeparture}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${inbound.startStopName} - ${inbound.endStopName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                    )
                    Text(
                        text = "${inbound.firstDeparture} - ${inbound.lastDeparture}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                    )
                }
            }

            // ETA seccion
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = etaResult,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryFixedVariant
                    )
                }

                Button(
                    onClick = onCalculateEtaClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                        contentColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val buttonText =
                        if (etaResult.contains("Llega")) {
                            "Re-Calcular ETA"
                        } else {
                            "Calcular Eta"
                        }
                    Text(buttonText)
                }
            }
        }
    }
}
