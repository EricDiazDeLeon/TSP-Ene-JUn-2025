package com.example.mirutadigital.ui.screens.routes.activeRoutes


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mirutadigital.ui.components.routes.ActiveRouteItem
import com.example.mirutadigital.viewModel.MapStateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActiveRoutesScreen(
    viewModel: ActiveRoutesViewModel,
    mapStateViewModel: MapStateViewModel,
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
            ActiveRoutesSheetHeader(
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
        }
        items(uiState.filteredRoutes, key = { it.id }) { route ->
            ActiveRouteItem(
                route = route,
                isFavorite = viewModel.isFavorite(route.id),
                isExpanded = route.id == expandedRouteId,
                onItemClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    val previouslyExpanded = expandedRouteId == route.id
                    expandedRouteId = if (previouslyExpanded) null else route.id

                    mapStateViewModel.showAllStops()

                    if (!previouslyExpanded) {
                        scope.launch {
                            val index =
                                uiState.filteredRoutes.indexOfFirst { it.id == route.id }

                            val fullRouteInfo = viewModel.getFullRouteInfo(route.id) // la debo llamar desde una remebre cocoutinescope por que ahora es suspend
                            if (fullRouteInfo != null) {
                                mapStateViewModel.showRouteDetailFromInfo(fullRouteInfo)
                            }

                            if (index != -1) {
                                delay(100)
                                lazyListState.animateScrollToItem(index)
                            }
                        }
                    }
                },
                onToggleFavorite = { viewModel.toggleFavorite(route.id) },
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
    onSearchQueryChange: (String) -> Unit,
    showOnlyFavorites: Boolean = false,
    onToggleShowFavorites: () -> Unit = {},
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
                modifier = Modifier.weight(1f)
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
