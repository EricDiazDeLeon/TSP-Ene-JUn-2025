package com.example.mirutadigital.ui.screens.share

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mirutadigital.navigation.AppScreens
import com.example.mirutadigital.ui.components.routes.RouteItem
import com.example.mirutadigital.ui.util.ShareManager
import com.example.mirutadigital.ui.util.SnackbarManager
import com.example.mirutadigital.viewModel.LocationViewModel
import com.example.mirutadigital.viewModel.MapStateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
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
        val selectedRoute = uiState.routes.find { it.id == uiState.expandedRouteId }
        val hasOutbound = selectedRoute?.stopsJourney?.getOrNull(0)?.stops?.isNotEmpty() == true
        val hasInbound = selectedRoute?.stopsJourney?.getOrNull(1)?.stops?.isNotEmpty() == true

        JourneySelectionDialog(
            onDismiss = { viewModel.onDismissJourneyDialog() },
            hasOutbound = hasOutbound,
            hasInbound = hasInbound,
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
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            val lazyListState = rememberLazyListState()
                            val scope = rememberCoroutineScope()

                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(uiState.filteredRoutes, key = { it.id }) { route ->
                                    RouteItem(
                                        route = route,
                                        isFavorite = viewModel.isFavorite(route.id),
                                        isExpanded = uiState.expandedRouteId == route.id,
                                        onItemClick = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            val previouslyExpanded = uiState.expandedRouteId == route.id
                                            viewModel.toggleExpand(route.id)

                                            if (!previouslyExpanded) {
                                                scope.launch {
                                                    val index = uiState.filteredRoutes.indexOfFirst { it.id == route.id }
                                                    if (index != -1) {
                                                        delay(100)
                                                        lazyListState.animateScrollToItem(index)
                                                    }
                                                }
                                            }
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
            text = "Actualmente compartiendo: $routeName",
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
    hasOutbound: Boolean,
    hasInbound: Boolean,
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

                // ida
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (hasOutbound) {
                                Modifier.clickable { selectedJourney = "outbound" }
                            } else {
                                Modifier
                            }
                        )
                        .padding(8.dp)
                        .alpha(if (hasOutbound) 1f else 0.4f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedJourney == "outbound",
                        onClick = if (hasOutbound) {
                            { selectedJourney = "outbound" }
                        } else null,
                        enabled = hasOutbound
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.Blue, CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("De Ida", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // vuelta
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (hasInbound) {
                                Modifier.clickable { selectedJourney = "inbound" }
                            } else {
                                Modifier
                            }
                        )
                        .padding(8.dp)
                        .alpha(if (hasInbound) 1f else 0.4f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedJourney == "inbound",
                        onClick = if (hasInbound) {
                            { selectedJourney = "inbound" }
                        } else null,
                        enabled = hasInbound
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.Red, CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("De Vuelta", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedJourney?.let { onConfirm(it) }
                },
                enabled = selectedJourney != null
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
