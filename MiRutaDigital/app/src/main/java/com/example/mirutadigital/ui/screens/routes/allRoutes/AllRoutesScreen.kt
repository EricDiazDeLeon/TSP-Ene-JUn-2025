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
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.material3.IconButton
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
//import com.example.mirutadigital.data.testsUi.dataSource.RoutesInfo
//import com.example.mirutadigital.data.testsUi.model.Stop

import com.example.mirutadigital.data.model.ui.RoutesInfo
import com.example.mirutadigital.data.model.ui.base.Stop
import com.example.mirutadigital.ui.components.routes.RouteItem

import com.example.mirutadigital.viewModel.MapStateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllRoutesScreen(
    viewModel: AllRoutesViewModel = viewModel(),
    mapStateViewModel: MapStateViewModel,
    onViewRouteClick: (String) -> Unit,
    isSheetExpanded: Boolean,
    onExpandSheet: () -> Unit
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
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    showOnlyFavorites = uiState.showOnlyFavorites,
                    onToggleShowFavorites = viewModel::toggleShowOnlyFavorites,
                    isSheetExpanded = isSheetExpanded,
                    onExpandSheet = onExpandSheet
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
                isFavorite = viewModel.isFavorite(route.id),
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
                        mapStateViewModel.showRouteDetailFromInfo(route) // camnio
                    }

                    if (!previouslyExpanded) {
                        scope.launch {
                            val index =
                                uiState.filteredRoutes.indexOfFirst { it.id == route.id }
                            if (index != -1) {
                                delay(100)
                                if (uiState.filteredStopId != null) {
                                    lazyListState.animateScrollToItem(index, scrollOffset = -115)
                                } else {
                                    lazyListState.animateScrollToItem(index)
                                }
                            }
                        }
                    }
                },
                onToggleFavorite = { viewModel.toggleFavorite(route.id) },
                onViewRouteClick = { onViewRouteClick(route.id) }
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
    onSearchQueryChange: (String) -> Unit,
    showOnlyFavorites: Boolean,
    onToggleShowFavorites: () -> Unit,
    isSheetExpanded: Boolean,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onExpandSheet,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSecondaryFixedVariant
                ),
                contentPadding = PaddingValues(horizontal = 29.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.DirectionsBus, contentDescription = "Rutas")
                Spacer(
                    modifier = Modifier
                        .width(6.dp)
                        .height(40.dp)
                )
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

            IconButton(
                onClick = onToggleShowFavorites,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (showOnlyFavorites) "Ver todas" else "Solo favoritas",
                    tint = if (showOnlyFavorites) MaterialTheme.colorScheme.onSecondaryFixedVariant else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
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
