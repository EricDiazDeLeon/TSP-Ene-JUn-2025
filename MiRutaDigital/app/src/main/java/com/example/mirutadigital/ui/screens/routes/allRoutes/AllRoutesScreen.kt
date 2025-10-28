package com.example.mirutadigital.ui.screens.routes.allRoutes


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.mirutadigital.data.testsUi.dataSource.RouteInfoSchedulel
import com.example.mirutadigital.data.testsUi.dataSource.RoutesInfo
import com.example.mirutadigital.data.testsUi.model.Stop
import com.example.mirutadigital.viewModel.MapStateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllRoutesScreen(
    viewModel: AllRoutesViewModel = viewModel(),
    mapStateViewModel: MapStateViewModel
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var expandedRouteId by remember { mutableStateOf<String?>(null) }
    //var selectedItemIndex by remember { mutableIntStateOf(1) }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                AllRoutesSheetHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange
                )

                AnimatedVisibility(
                    visible = uiState.filteredStopId != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    FilterChip(
                        stopName = uiState.filteredStopName ?: "",
                        onClearFilter = {
                            viewModel.clearStopFilter()
                            mapStateViewModel.showAllStops()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 0.dp)
                    )
                }
            }
        }

        items(uiState.filteredRoutes, key = { it.id }) { route ->
            RouteItem(
                route = route,
                isExpanded = route.id == expandedRouteId,
                onItemClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    val previouslyExpanded = expandedRouteId == route.id
                    expandedRouteId = if (previouslyExpanded) null else route.id

                    if (previouslyExpanded) {
                        if (uiState.filteredStopId != null) {
                            mapStateViewModel.showSingleStopFocus(uiState.filteredStopId!!)
                        } else {
                            mapStateViewModel.showAllStops()
                        }
                    } else {
                        mapStateViewModel.showRouteDetail(route)
                    }

                    if (!previouslyExpanded) {
                        scope.launch {
                            val index =
                                uiState.filteredRoutes.indexOfFirst { it.id == route.id }
                            if (index != -1) {
                                delay(100)
                                lazyListState.animateScrollToItem(index)
                            }
                        }
                    }
                },
            )
        }
        item {
            if (uiState.filteredRoutes.isEmpty() && !uiState.isLoading) {
                Text(
                    "No se encontraron rutas...",
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            } else {
                Text(
                    "No hay más rutas...", modifier = Modifier
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

@Composable
fun AllRoutesSheetHeader(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSecondaryFixedVariant
                ),
                contentPadding = PaddingValues(horizontal = 29.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.DirectionsBus, contentDescription = "Rutas")
                Spacer(modifier = Modifier
                    .width(6.dp)
                    .height(40.dp))
                Text(text = "Rutas")
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
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun FilterChip(
    stopName: String,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Filtrado por:",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stopName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onClearFilter,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                contentColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Quitar filtro",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Quitar filtro",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
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
fun RouteItem(
    route: RoutesInfo,
    isExpanded: Boolean,
    onItemClick: () -> Unit
) {
    // outboundJourney inicio medio y fin
    val outbound =
        formatJourneyStops(route.stopsJourney.getOrNull(0)?.stops)
    // inboundJourney inicio medio y fin
    val inbound =
        formatJourneyStops(route.stopsJourney.getOrNull(1)?.stops)

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
                        text = route.name,
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
                                //modifier = Modifier.weight(1f),
                                onClick = { "TODO: Ver Ruta" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    contentColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
