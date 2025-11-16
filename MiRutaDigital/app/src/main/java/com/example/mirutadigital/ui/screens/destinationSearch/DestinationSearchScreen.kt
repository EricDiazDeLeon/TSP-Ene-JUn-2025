package com.example.mirutadigital.ui.screens.destinationSearch

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mirutadigital.data.model.ui.RouteInfo
import com.example.mirutadigital.viewModel.MapStateViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DestinationSearchScreen(
    navController: NavController,
    mapStateViewModel: MapStateViewModel,
    isSheetExpanded: Boolean,
    onExpandSheet: () -> Unit,
    viewModel: DestinationSearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.searchResult) {
        val result = uiState.searchResult
        if (result is SearchResult.Success) {
            // mapStateViewModel.setTemporaryDestination(result.destination)
            mapStateViewModel.showSingleStopFocus(result.closestStop.id)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
    ) {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                DestinationSearchHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onSearchClick = viewModel::searchDestination,
                    isLoading = uiState.isLoading,
                    isSheetExpanded = isSheetExpanded,
                    onExpandSheet = onExpandSheet
                )

                if (uiState.searchResult is SearchResult.Success) {
                    val succesResult = uiState.searchResult as SearchResult.Success
                    ClosestStopHeader(
                        stopName = succesResult.closestStop.name,
                        distance = succesResult.closestStop.distance,
                        destinationName = succesResult.destinationName
                    )
                }
            }

        }

        item {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        when (val result = uiState.searchResult) {
            is SearchResult.Success -> {
                items(result.closestStop.routes) { route ->
                    RouteResultItem(route = route)
                }
                item {
                    Text(
                        "No hay más rutas...", modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }

            is SearchResult.NoResult -> {
                item { EmptyStateMessage("No se encontró una parada cercana a ese destino.") }
            }

            is SearchResult.Error -> {
                item { EmptyStateMessage("Error al buscar. Revisa tu conexión o la dirección.") }
            }

            null -> {
                item { EmptyStateMessage("Busca una dirección para encontrar la parada mas cercana y sus rutas.") }
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
            .padding(horizontal = 12.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Text(
            text = message,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSecondaryFixedVariant
        )
    }
}

@Composable
fun DestinationSearchHeader(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    isLoading: Boolean,
    isSheetExpanded: Boolean,
    onExpandSheet: () -> Unit
) {
    Column(modifier = modifier) {
        Crossfade(
            targetState = isSheetExpanded,
            animationSpec = tween(durationMillis = 400),
            label = "SearchHeaderAnimation"
        ) { expanded ->
            if (expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .padding(top = 4.dp, bottom = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Introduce tu destino", color = Color.Gray)},
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
                    Button(
                        modifier = Modifier.height(56.dp),
                        onClick = onSearchClick,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                            contentColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            } else {
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
                    Icon(Icons.Default.Search, contentDescription = "Buscar Destino")
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Buscar Destino")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ClosestStopHeader(
    stopName: String,
    distance: String,
    destinationName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Text(
            text = destinationName,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.BackHand,
            contentDescription = "Parada Cercana",
            tint = MaterialTheme.colorScheme.onSecondaryFixedVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = stopName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryFixedVariant
            )
            Text(
                text = distance,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RouteResultItem(route: RouteInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(12.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(start = 16.dp),
            imageVector = Icons.Default.DirectionsBus,
            contentDescription = "Rutas",
            tint = MaterialTheme.colorScheme.onSecondaryFixedVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = route.name,
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .weight(0.2f),
            maxLines = 1
        )
        Text(
            text = route.destination,
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .weight(0.5f),
            color = Color.Gray,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}
