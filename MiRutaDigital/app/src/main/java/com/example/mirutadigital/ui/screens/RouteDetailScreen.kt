package com.example.mirutadigital.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.viewModel.RouteViewModel

@Composable
fun RouteDetailScreen(
    routeId: String,
    viewModel: RouteViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is com.example.mirutadigital.viewModel.RouteUiState.Loading -> {
            LoadingScreen()
        }
        is com.example.mirutadigital.viewModel.RouteUiState.Success -> {
            val route = state.routes.find { it.id == routeId }
            route?.let {
                RouteDetailContent(
                    route = it,
                    onToggleFavorite = { viewModel.toggleFavorite(routeId) }
                )
            } ?: ErrorScreen("Ruta no encontrada")
        }
        is com.example.mirutadigital.viewModel.RouteUiState.Error -> {
            ErrorScreen(state.message)
        }
    }
}

@Composable
fun RouteDetailContent(
    route: Route,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = route.name,
                style = MaterialTheme.typography.headlineMedium
            )
            
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (route.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (route.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                    tint = if (route.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Horario: ${route.operatingHours}",
            style = MaterialTheme.typography.bodyLarge
        )
        
        // Aquí se podría añadir un mapa con la ruta
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message)
    }
}