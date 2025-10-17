package com.example.mirutadigital.ui.screens.routes

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.repository.AppRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun RoutesScreen(
    navController: NavController,
    repository: AppRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val routes by repository.getRoutes().collectAsState(initial = emptyList())

    var selectedRoute by remember { mutableStateOf<Route?>(null) }
    val cameraPositionState = rememberCameraPositionState()

    val currentLocation = remember { mutableStateOf<LatLng?>(null) }

    // 🔹 Lanza el permiso de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                try {
                    val locationProvider = LocationServices.getFusedLocationProviderClient(context)
                    val location = locationProvider.lastLocation.await()
                    currentLocation.value = LatLng(location.latitude, location.longitude)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 🔹 Verificar permiso de ubicación y obtener ubicación actual
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationProvider = LocationServices.getFusedLocationProviderClient(context)
            try {
                val location = locationProvider.lastLocation.await()
                currentLocation.value = LatLng(location.latitude, location.longitude)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Rutas disponibles",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(routes) { route ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selectedRoute = route
                            coroutineScope.launch {
                                if (route.polylinePoints.isNotEmpty()) {
                                    val firstPoint = route.polylinePoints.first()
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(firstPoint, 13f)
                                    )
                                }
                            }
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(route.name, style = MaterialTheme.typography.titleMedium)
                        Text("Horario: ${route.operatingHours}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Box(Modifier.fillMaxWidth().height(300.dp)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = currentLocation.value != null)
            ) {
                selectedRoute?.let { route ->
                    Polyline(points = route.polylinePoints)
                }

                currentLocation.value?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Tu ubicación"
                    )
                }
            }
        }
    }
}
