package com.example.mirutadigital.ui.screens.streetview

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.mirutadigital.ui.util.SnackbarManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.StreetViewPanoramaView
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

@Composable
fun StreetViewScreen(
    coords: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val parts = coords.split(",")
    val lat = parts.getOrNull(0)?.toDoubleOrNull()
    val lng = parts.getOrNull(1)?.toDoubleOrNull()

    var isLoading by remember { mutableStateOf(true) }
    var coverageError by remember { mutableStateOf(false) }
    var internetError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val connected = isNetworkAvailable(context)
        if (!connected) {
            internetError = true
            SnackbarManager.showMessage("Se requiere internet para cargar la vista calle.")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (lat != null && lng != null && !internetError) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    StreetViewPanoramaView(ctx).apply {
                        onCreate(Bundle())
                        getStreetViewPanoramaAsync { panorama: StreetViewPanorama ->
                            panorama.setOnStreetViewPanoramaChangeListener { location ->
                                isLoading = false
                                coverageError = location == null
                                if (coverageError) {
                                    scope.launch {
                                        SnackbarManager.showMessage("La vista a nivel de calle no está disponible para esta ubicación.")
                                    }
                                }
                            }
                            panorama.setPosition(LatLng(lat, lng))
                            panorama.setUserNavigationEnabled(false)
                        }
                        onResume()
                    }
                },
                onRelease = { view ->
                    view.onPause()
                    view.onDestroy()
                }
            )
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (coverageError || internetError) {
            Text(
                text = if (internetError) "Sin conexión a internet" else "Sin cobertura de Street View",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}
