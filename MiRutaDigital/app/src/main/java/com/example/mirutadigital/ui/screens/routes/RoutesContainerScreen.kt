package com.example.mirutadigital.ui.screens.routes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mirutadigital.data.repository.AppRepository
import com.example.mirutadigital.ui.screens.routes.activeRoutes.ActiveRoutesScreen
import com.example.mirutadigital.ui.screens.routes.activeRoutes.ActiveRoutesViewModel
import com.example.mirutadigital.ui.screens.routes.activeRoutes.ActiveRoutesViewModelFactory
import com.example.mirutadigital.ui.screens.routes.allRoutes.AllRoutesScreen
import com.example.mirutadigital.ui.screens.routes.allRoutes.AllRoutesViewModel
import com.example.mirutadigital.ui.screens.routes.allRoutes.AllRoutesViewModelFactory
import com.example.mirutadigital.ui.screens.routes.RoutesContainerViewModelFactory
import com.example.mirutadigital.viewModel.MapStateViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RoutesContainerScreen(
    repository: AppRepository,
    activeRoutesViewModel: ActiveRoutesViewModel = viewModel(factory = ActiveRoutesViewModelFactory(repository)),
    allRoutesViewModel: AllRoutesViewModel = viewModel(factory = AllRoutesViewModelFactory(repository)),
    containerViewModel: RoutesContainerViewModel = viewModel(factory = RoutesContainerViewModelFactory(repository)),
    mapStateViewModel: MapStateViewModel,
    filteredStopId: String? = null
) {
    val uiState by containerViewModel.uiState.collectAsState()

    val allRoutesUiState by allRoutesViewModel.uiState.collectAsState()

    LaunchedEffect(filteredStopId) {
        if (filteredStopId != null) {
            allRoutesViewModel.setStopFilter(filteredStopId)

            //mapStateViewModel.showSingleStopFocus(filteredStopId)
        }
    }

    LaunchedEffect(uiState.currentView, allRoutesUiState.filteredStopId) {
        when (uiState.currentView) {
            RouteView.ALL -> {
                // foco al mapa si habia un filtro o se aplica
                val currentFilterId = allRoutesUiState.filteredStopId
                if (currentFilterId != null) {
                    mapStateViewModel.showSingleStopFocus(currentFilterId)
                } else {
                    mapStateViewModel.showAllStops()
                }
            }

            RouteView.ACTIVE -> {
                mapStateViewModel.showAllStops()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        when (uiState.currentView) {
            RouteView.ALL -> {
                AllRoutesScreen(
                    viewModel = allRoutesViewModel,
                    mapStateViewModel = mapStateViewModel
                )
            }

            RouteView.ACTIVE -> {
                ActiveRoutesScreen(
                    viewModel = activeRoutesViewModel,
                    mapStateViewModel = mapStateViewModel
                )
            }
        }

        // boton flotante fab
        FloatingActionButton(
            onClick = {
                containerViewModel.toggleView()
                //mapStateViewModel.showAllStops()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
            contentColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            // icono diferente segun la vista actual
            val icon = if (uiState.currentView == RouteView.ACTIVE) {
                Icons.Default.DirectionsBus
            } else {
                Icons.Default.Schedule
            }
            val description = if (uiState.currentView == RouteView.ACTIVE) {
                "Ver rutas"
            } else {
                "Ver rutas activas"
            }

            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}
