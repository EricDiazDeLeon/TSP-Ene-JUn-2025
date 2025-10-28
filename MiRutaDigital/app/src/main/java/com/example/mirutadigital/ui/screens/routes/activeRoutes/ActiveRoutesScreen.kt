package com.example.mirutadigital.ui.screens.routes.activeRoutes


import android.R
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.example.mirutadigital.data.testsUi.dataSource.RouteInfoSchedulel
import com.example.mirutadigital.viewModel.MapStateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActiveRoutesScreen(
    viewModel: ActiveRoutesViewModel, //= viewModel()
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
            ActiveRoutesSheetHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange
            )
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

                    mapStateViewModel.showAllStops()

                    if (!previouslyExpanded) {
//                        viewModel.onRouteExpanded(route) // para cuando se ocupe mostrar en el mapa
                        scope.launch {
                            val index =
                                uiState.filteredRoutes.indexOfFirst { it.id == route.id }

                            val fullRouteInfo = viewModel.getFullRouteInfo(route.id)
                            if (fullRouteInfo != null) {
                                mapStateViewModel.showRouteDetail(fullRouteInfo)
                            }

                            if (index != -1) {
                                delay(100)
                                lazyListState.animateScrollToItem(index)
                            }
                        }
                    }
                }
            )
        }
        item {
            if (uiState.filteredRoutes.isEmpty() && !uiState.isLoading) {
                Text(
                    "No se encontraron rutas activas...", modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            } else {
                Text(
                    "No hay más rutas activas...", modifier = Modifier
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
fun ActiveRoutesSheetHeader(
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
                contentPadding = PaddingValues(horizontal = 23.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = "Rutas Activas")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Rutas\nActivas")
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
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteItem(
    route: RouteInfoSchedulel,
    isExpanded: Boolean,
    onItemClick: () -> Unit
) {
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
                ) // icono de la ruta
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.name,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSecondaryFixedVariant
                    )
                    Row {
                        Text(
                            text = "Horario:",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        if (!isExpanded) {
                            val textExpanded =
                                "${route.outboundInfo.schedule} ' ${route.inboundInfo.schedule}"
                            //route.schedulesInfo.joinToString(separator = " ' ") { it.schedule }

                            Text(
                                text = textExpanded,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir/Colapsar",
                    tint = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 2.dp)
                Spacer(modifier = Modifier.width(16.dp))

                JourneyInfo(
                    originName = route.outboundInfo.nameOrigin,
                    destinationName = route.outboundInfo.nameDestination,
                    firstDeparture = route.outboundInfo.schedule.first,
                    lastDeparture = route.outboundInfo.schedule.second,
                    colorLine = Color.Blue.copy(alpha = 0.7f)
                )

                JourneyInfo(
                    originName = route.inboundInfo.nameOrigin,
                    destinationName = route.inboundInfo.nameDestination,
                    firstDeparture = route.inboundInfo.schedule.first,
                    lastDeparture = route.inboundInfo.schedule.second,
                    colorLine = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}


@Composable
fun JourneyInfo(
    originName: String,
    destinationName: String,
    firstDeparture: String,
    lastDeparture: String,
    colorLine: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        // Información de salida
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = originName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.25f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colorLine,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(14.dp)
            )
            Text(
                text = destinationName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.25f)
            )

            Text(
                text = firstDeparture,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.weight(0.1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colorLine,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(14.dp)
            )
            Text(
                text = lastDeparture,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.weight(0.1f)
            )
        }
    }
}