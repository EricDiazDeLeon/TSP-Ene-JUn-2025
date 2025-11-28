package com.example.mirutadigital.ui.screens.streetview

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mirutadigital.ui.util.SnackbarManager
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.StreetViewPanoramaView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.StreetViewPanoramaLocation
import kotlinx.coroutines.delay

@Composable
fun StreetViewScreen(
    coords: String,
    onNavigateBack: () -> Unit,
    viewModel: StreetViewViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(uiState.hasCoverageError) {
        if (uiState.hasCoverageError) {
            onNavigateBack()
            SnackbarManager.showMessage("No hay vista de calle disponible para esta parada")
        }
    }

    LaunchedEffect(coords) {
        viewModel.initialize(coords)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.lat != null && uiState.lng != null && !uiState.hasInternetError && !uiState.hasCoverageError) {
            val streetViewPanoramaView = remember {
                StreetViewPanoramaView(context).apply {
                    onCreate(Bundle())
                }
            }

            DisposableEffect(lifecycleOwner) {
                val lifecycleObserver = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> streetViewPanoramaView.onResume()
                        Lifecycle.Event.ON_PAUSE -> streetViewPanoramaView.onPause()
                        Lifecycle.Event.ON_DESTROY -> streetViewPanoramaView.onDestroy()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
                    streetViewPanoramaView.onDestroy()
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { streetViewPanoramaView },
                update = { view ->
                    view.getStreetViewPanoramaAsync { panorama: StreetViewPanorama ->
                        panorama.setOnStreetViewPanoramaChangeListener { location: StreetViewPanoramaLocation? ->
                            if (location != null) {
                                viewModel.onPanoramaLoaded()
                            } else {
                                viewModel.onPanoramaFailed()
                            }
                        }
                        panorama.setPosition(LatLng(uiState.lat!!, uiState.lng!!))
                        // Permitir paneo (mirar alrededor)
                        panorama.setUserNavigationEnabled(true) 
                        panorama.isPanningGesturesEnabled = true
                        panorama.isZoomGesturesEnabled = true
                    }
                }
            )
        }

        FloatingActionButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 60.dp),
            containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
            contentColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver"
            )
        }

        if (uiState.isLoading && !uiState.hasCoverageError && !uiState.hasInternetError) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (uiState.hasCoverageError || uiState.hasInternetError) {
            Text(
                text = if (uiState.hasInternetError) "Sin conexión a internet" else "Sin cobertura de Street View",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
