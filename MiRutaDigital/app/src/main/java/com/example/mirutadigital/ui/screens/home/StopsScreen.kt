package com.example.mirutadigital.ui.screens.home

import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Streetview
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableTargetMarker
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mirutadigital.data.model.ui.StopWithRoutes
import com.example.mirutadigital.navigation.AppScreens
import com.example.mirutadigital.viewModel.LocationViewModel
import com.example.mirutadigital.viewModel.MapStateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StopsScreen(
    viewModel: StopsViewModel = viewModel(),
    mapStateViewModel: MapStateViewModel,
    locationViewModel: LocationViewModel,
    navController: NavHostController,
    isSheetExpanded: Boolean,
    onExpandSheet: () -> Unit,
    onNavigateToStreetView: (Double, Double) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // observa el estado completo de la ui desde el vm
    val uiState by viewModel.uiState.collectAsState()
    val userLocation by locationViewModel.location.collectAsState()

    // solo actualiza la ubicacin
    LaunchedEffect(userLocation) {
        viewModel.updateUserLocation(userLocation)
    }

    val mapState by mapStateViewModel.mapState.collectAsState()
    val expandedStopId = mapState.selectedStopId

    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(expandedStopId) {
        if (expandedStopId != null) {
            val index = uiState.filteredStops.indexOfFirst { it.id == expandedStopId }
            if (index != -1) {
                launch {
                    delay(100)
                    //lazyListState.animateScrollToItem(index = index)
                    lazyListState.scrollToItem(index = index)
                }
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        stickyHeader {
            StopsSheetHeader(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                isSorted = uiState.isSortedByLocation,
                onSortClick = {
                    if (uiState.isSortedByLocation) {
                        viewModel.resetStopsOrder()
                    } else {
                        val closestStop = viewModel.sortStopsByProximity()
                        if (closestStop != null) {
                            mapStateViewModel.setSelectedStop(closestStop.id)
                        }
                    }
                },
                isSheetExpanded = isSheetExpanded,
                onExpandSheet = onExpandSheet
            )
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(uiState.filteredStops, key = { it.id }) { stop ->
                StopItem(
                    stop = stop,
                    isExpanded = stop.id == expandedStopId,
                    onItemClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()

                        val previouslyExpanded = expandedStopId == stop.id
                        mapStateViewModel.setSelectedStop(
                            if (previouslyExpanded) null else stop.id
                        )

                        if (!previouslyExpanded) {
                            scope.launch {
                                val index =
                                    uiState.filteredStops.indexOfFirst { it.id == stop.id }
                                if (index != -1) {
                                    delay(100)
                                    lazyListState.animateScrollToItem(index)
                                }
                            }
                        }
                    },
                    userLocation = userLocation,
                    onViewRoutesClick = { stopId ->
                        val routeToNavigate = AppScreens.AllRoutesScreen.createRoute(stopId)
                        navController.navigate(routeToNavigate) {
                            launchSingleTop = true
                        }
                    },
                    onStreetViewClick = onNavigateToStreetView
                )
            }

            item {
                if (uiState.filteredStops.isEmpty()) {
                    Text(
                        "No se encontraron paradas...", modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        "No hay más paradas...", modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@Composable
fun StopsSheetHeader(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSorted: Boolean,
    onSortClick: () -> Unit,
    isSheetExpanded: Boolean,
    onExpandSheet: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = if (!isSheetExpanded) onExpandSheet else onSortClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                    contentColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FrontHand,
                    contentDescription = "Paradas Cercanas"
                )
                Spacer(
                    modifier = Modifier
                        .width(6.dp)
                        .height(40.dp)
                )
                Text(if (isSorted) "Paradas\nsin orden" else "Paradas\nCercanas")
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSecondaryFixedVariant
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        enabled = !isSheetExpanded,
                        onClick = onExpandSheet
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                enabled = isSheetExpanded,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Borrar texto"
                            )
                        }
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopItem(
    stop: StopWithRoutes,
    isExpanded: Boolean,
    onItemClick: () -> Unit,
    userLocation: Location?,
    onViewRoutesClick: (String) -> Unit,
    onStreetViewClick: (Double, Double) -> Unit
) {
    val distanceText by remember(userLocation) {
        mutableStateOf(
            if (userLocation == null) {
                "Ubicación no disponible"
            } else {
                val stopLocation = Location("").apply {
                    latitude = stop.latitude
                    longitude = stop.longitude
                }
                val distanceInMeters = userLocation.distanceTo(stopLocation).toInt()

                when {
                    distanceInMeters < 1000 -> "Desde tu ubicación a $distanceInMeters metros"
                    else -> {
                        val distanceInKm = distanceInMeters / 1000.0
                        "Desde tu ubicación a %.1f km".format(distanceInKm)
                    }
                }
            }
        )
    }

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
                .padding(16.dp)
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
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stop.name,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSecondaryFixedVariant
                    )
                    Text(
                        text = distanceText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir/Colapsar",
                    tint = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 2.dp)
                if (stop.routes.isNotEmpty()) {
                    val maxRoutesToShow = 3
                    val routesToShow = stop.routes.take(maxRoutesToShow)

                    routesToShow.forEach { route ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {

                            Text(
                                text = route.name,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                            )
                            Spacer(modifier = Modifier.weight(0.1f))
                            Text(
                                text = route.destination,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (stop.routes.size > maxRoutesToShow) {
                            val moreOneAddS =
                                if (stop.routes.size > maxRoutesToShow + 1) "s" else ""
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "${stop.routes.size - maxRoutesToShow} ruta$moreOneAddS más",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { onStreetViewClick(stop.latitude, stop.longitude) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                contentColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Streetview,
                                contentDescription = "Street View"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Vista de Calle", maxLines = 1)
                        }
                        Spacer(modifier = Modifier.padding(end = 4.dp))
                        Button(
                            onClick = { onViewRoutesClick(stop.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                contentColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Text("Ver Rutas", maxLines = 1)
                        }
                    }
                } else {
                    Text("No hay rutas en esta parada")
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
        }
    }
}