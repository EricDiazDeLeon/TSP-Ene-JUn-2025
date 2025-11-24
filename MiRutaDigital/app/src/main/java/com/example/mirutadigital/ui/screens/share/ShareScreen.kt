package com.example.mirutadigital.ui.screens.share

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mirutadigital.data.model.ui.RoutesInfo
import com.example.mirutadigital.data.model.ui.base.Stop
import com.example.mirutadigital.navigation.AppScreens
import com.example.mirutadigital.navigation.Routes
import com.example.mirutadigital.ui.util.ShareManager
import com.example.mirutadigital.ui.util.SnackbarManager
import com.example.mirutadigital.viewModel.LocationViewModel
import com.example.mirutadigital.viewModel.MapStateViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShareScreen(
    navController: NavController,
    locationViewModel: LocationViewModel,
    mapStateViewModel: MapStateViewModel,
    isSheetExpanded: Boolean,
    onExpandSheet: () -> Unit,
    viewModel: ShareViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val location by locationViewModel.location.collectAsState()
    val isSharing by ShareManager.currentSharedRouteId.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState.expandedRouteId) {
        val routeId = uiState.expandedRouteId
        if (routeId == null) {
            mapStateViewModel.showAllStops()
        } else {
            val route = uiState.routes.find { it.id == routeId }
            route?.let { mapStateViewModel.showRouteDetailFromInfo(it) }
        }
    }

    if (uiState.showJourneyDialog) {
        JourneySelectionDialog(
            onDismiss = { viewModel.onDismissJourneyDialog() },
            onConfirm = { journeyType ->
                val loc = location
                if (loc == null) {
                    viewModel.viewModelScope.launch { SnackbarManager.showMessage("No se pudo obtener la ubicación.") }
                    return@JourneySelectionDialog
                }
                viewModel.onConfirmShare(loc, journeyType)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        if (isSharing != null) {
            SharingActiveView(
                routeName = uiState.routes.find { it.id == isSharing }?.name ?: "Ruta",
                onStopSharing = { viewModel.stopShare() }
            )
        } else {
            Crossfade(
                targetState = isSheetExpanded,
                animationSpec = tween(300),
                label = "ShareScreenToggle",
                modifier = Modifier.fillMaxSize()
            ) { expanded ->
                if (!expanded) {
                    ShareSheetHeaderCollapsed(
                        onClick = onExpandSheet,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ShareSheetHeaderExpanded(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow),
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = viewModel::onSearchQueryChange,
                            showOnlyFavorites = uiState.showOnlyFavorites,
                            onToggleShowFavorites = viewModel::toggleShowOnlyFavorites
                        )

                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier.weight(1f), // Ocupa el espacio restante
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            val lazyListState = rememberLazyListState()

                            // Lista de rutas
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(uiState.filteredRoutes, key = { it.id }) { route ->
                RouteItemCard(
                                        route = route,
                                        isFavorite = viewModel.isFavorite(route.id),
                                        isExpanded = uiState.expandedRouteId == route.id,
                                        onItemClick = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            viewModel.toggleExpand(route.id)
                                        },
                                        onToggleFavorite = { viewModel.toggleFavorite(route.id) },
                                        onViewRouteClick = { routeId ->
                                            if (routeId.isNotBlank()) {
                                                navController.navigate(
                                                    AppScreens.RouteDetailScreen.createRoute(routeId)
                                                )
                                            } else {
                                                viewModel.viewModelScope.launch {
                                                    SnackbarManager.showMessage("Error: No se puede abrir esta ruta (ID inválido)")
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (location == null) {
                                        viewModel.viewModelScope.launch { SnackbarManager.showMessage("No tienes ubicación disponible") }
                                        return@Button
                                    }
                                    if (uiState.expandedRouteId == null) {
                                        viewModel.viewModelScope.launch { SnackbarManager.showMessage("Primero selecciona una ruta de la lista") }
                                        return@Button
                                    }
                                    viewModel.onShareClick()
                                },
                                enabled = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    contentColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Compartir")
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (uiState.expandedRouteId != null) "Compartir Ruta Seleccionada"
                                    else "Selecciona una Ruta"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SharingActiveView(
    routeName: String,
    onStopSharing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Estás compartiendo!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryFixedVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Actualmente compartiendo: ruta $routeName",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStopSharing,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Icon(Icons.Default.Clear, contentDescription = "Dejar de compartir")
            Spacer(Modifier.width(8.dp))
            Text("Dejar de Compartir")
        }
    }
}

@Composable
fun ShareSheetHeaderCollapsed(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSecondaryFixedVariant
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(Icons.Default.Share, contentDescription = "Compartir Ruta")
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Compartir Ruta",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheetHeaderExpanded(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showOnlyFavorites: Boolean,
    onToggleShowFavorites: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    "Buscar ruta...",
                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                focusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Borrar texto"
                        )
                    }
                }
            }
        )

        IconButton(
            onClick = onToggleShowFavorites,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (showOnlyFavorites) "Ver todas" else "Solo favoritas",
                tint = if (showOnlyFavorites) MaterialTheme.colorScheme.onSecondaryFixedVariant else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun JourneySelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (journeyType: String) -> Unit
) {
    var selectedJourney by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elige un Trayecto") },
        text = {
            Column {
                Text("¿Qué trayecto deseas compartir?")
                Spacer(modifier = Modifier.height(16.dp))

                // Opción Ida (Outbound)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedJourney = "outbound" }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedJourney == "outbound",
                        onClick = { selectedJourney = "outbound" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.Blue, CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("De Ida (Outbound)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Opción Vuelta (Inbound)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedJourney = "inbound" }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedJourney == "inbound",
                        onClick = { selectedJourney = "inbound" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.Red, CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("De Vuelta (Inbound)", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedJourney?.let { onConfirm(it) }
                },
                enabled = selectedJourney != null // Habilitado solo si se ha seleccionado un trayecto
            ) {
                Text("Compartir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatJourneyStops(stops: List<Stop>?): JourneyPoints {
    if (stops.isNullOrEmpty()) {
        return JourneyPoints("No disponible", "", "", 0)
    }

    val first = stops.first().name
    val middle = if (stops.size >= 3) stops[stops.size / 2].name else ""
    val last = if (stops.size >= 2) stops.last().name else ""

    return JourneyPoints(start = first, middle = middle, end = last, size = stops.size)
}

private data class JourneyPoints(
    val start: String,
    val middle: String,
    val end: String,
    val size: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteItemCard(
    route: RoutesInfo,
    isFavorite: Boolean,
    isExpanded: Boolean,
    onItemClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onViewRouteClick: (String) -> Unit
) {
    val outbound = formatJourneyStops(route.stopsJourney.getOrNull(0)?.stops)
    val inbound = formatJourneyStops(route.stopsJourney.getOrNull(1)?.stops)

    ElevatedCard(
        onClick = onItemClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .padding(start = 8.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                ) // icono de la ruta
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ruta " + route.name,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSecondaryFixedVariant
                    )
                    if (!isExpanded) {
                        // trayectos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = outbound.start,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    overflow = TextOverflow.Ellipsis, maxLines = 1
                                )
                                Text(
                                    text = outbound.end,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    overflow = TextOverflow.Ellipsis, maxLines = 1
                                )
                            }
                            VerticalDivider(
                                modifier = Modifier
                                    .height(24.dp)
                                    .padding(horizontal = 8.dp),
                                thickness = 1.dp,
                                color = Color.Gray.copy(alpha = 0.5f)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = inbound.start,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    overflow = TextOverflow.Ellipsis, maxLines = 1
                                )
                                Text(
                                    text = inbound.end,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    overflow = TextOverflow.Ellipsis, maxLines = 1
                                )
                            }
                        }
                    } else {
                        Text(
                            text = route.windshieldLabel,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            overflow = TextOverflow.Ellipsis, maxLines = 1
                        )
                        Text(
                            text = route.colors,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            overflow = TextOverflow.Ellipsis, maxLines = 1
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleFavorite() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (isFavorite) MaterialTheme.colorScheme.onSecondaryFixedVariant else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir/Colapsar",
                    tint = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(thickness = 2.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VerticalDivider(
                            modifier = Modifier
                                .height(35.dp)
                                .padding(horizontal = 8.dp),
                            thickness = 1.dp,
                            color = Color.Blue.copy(alpha = 0.5f)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1. " + outbound.start,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                overflow = TextOverflow.Ellipsis, maxLines = 1
                            )
                            if (outbound.size > 2) {
                                Text(
                                    text = "${outbound.size / 2 + 1}. " + outbound.middle,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    overflow = TextOverflow.Ellipsis, maxLines = 1
                                )
                            }
                            Text(
                                text = "${outbound.size}. " + outbound.end,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                overflow = TextOverflow.Ellipsis, maxLines = 1
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier
                                .height(35.dp)
                                .padding(horizontal = 8.dp),
                            thickness = 1.dp,
                            color = Color.Red.copy(alpha = 0.5f)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1. " + inbound.start,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                overflow = TextOverflow.Ellipsis, maxLines = 1
                            )
                            if (inbound.size > 2) {
                                Text(
                                    text = "${inbound.size / 2 + 1}. " + inbound.middle,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    overflow = TextOverflow.Ellipsis, maxLines = 1
                                )
                            }
                            Text(
                                text = "${inbound.size}. " + inbound.end,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                overflow = TextOverflow.Ellipsis, maxLines = 1
                            )
                        }

                        Column {
                            Button(
                                onClick = { onViewRouteClick(route.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    contentColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            ) {
                                Text("Ver Ruta")
                            }
                        }
                    }
                }
            }
        }
    }
}
