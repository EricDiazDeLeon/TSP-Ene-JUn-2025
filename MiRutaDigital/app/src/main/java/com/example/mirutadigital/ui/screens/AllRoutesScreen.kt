package com.example.mirutadigital.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.viewModel.RouteUiState
import com.example.mirutadigital.viewModel.RouteViewModel

@Composable
fun AllRoutesScreen(
    viewModel: RouteViewModel,
    onRouteClick: (String) -> Unit
) {
    var showOnlyFavorites by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(showOnlyFavorites) {
        if (showOnlyFavorites) {
            viewModel.getFavoriteRoutes()
        } else {
            viewModel.getAllRoutes()
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Filtro de favoritos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mostrar solo favoritos",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = showOnlyFavorites,
                onCheckedChange = { showOnlyFavorites = it }
            )
        }
        
        Divider()
        
        // Lista de rutas
        when (val state = uiState) {
            is RouteUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is RouteUiState.Success -> {
                if (state.routes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (showOnlyFavorites) 
                                "No tienes rutas favoritas" 
                            else 
                                "No hay rutas disponibles"
                        )
                    }
                } else {
                    LazyColumn {
                        items(state.routes) { route ->
                            RouteItem(
                                route = route,
                                onRouteClick = { onRouteClick(route.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(route.id) }
                            )
                        }
                    }
                }
            }
            is RouteUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message)
                }
            }
        }
    }
}

@Composable
fun RouteItem(
    route: Route,
    onRouteClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onRouteClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Horario: ${route.operatingHours}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (route.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (route.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                    tint = if (route.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}