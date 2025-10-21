package com.example.mirutadigital.ui.screens.shareLocation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.repository.AppRepository
import com.example.mirutadigital.ui.components.Toolbar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareLocationScreen(
    navController: NavController,
    repository: AppRepository,
    viewModel: ShareLocationViewModel = viewModel(
        factory = ShareLocationViewModelFactory(repository, LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showRouteDialog by remember { mutableStateOf(false) }

    // Permiso ahora se solicita al abrir la app en MainActivity

    // Mostrar snackbar para errores
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compartir Ubicación del Camión") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado de conexión
            ConnectionStatusCard(
                isNetworkAvailable = uiState.isNetworkAvailable,
                sharingStatus = uiState.sharingStatus
            )

            // Información del camión
            TruckInfoCard(
                truckId = uiState.truckId,
                currentLocation = uiState.currentLocation,
                lastUpdateTime = uiState.lastUpdateTime
            )

            // Selección de ruta
            RouteSelectionCard(
                selectedRoute = uiState.selectedRoute,
                onRouteSelected = { route ->
                    viewModel.onRouteSelected(route)
                    showRouteDialog = false
                },
                onShowRouteDialog = { showRouteDialog = true }
            )

            // Controles de compartir
            SharingControlsCard(
                isSharing = uiState.isSharing,
                isLocationPermissionGranted = uiState.isLocationPermissionGranted,
                selectedRoute = uiState.selectedRoute,
                sharingStatus = uiState.sharingStatus,
                onStartSharing = viewModel::startSharing,
                onStopSharing = viewModel::stopSharing
            )

            // Estado de compartir
            if (uiState.isSharing) {
                SharingStatusCard(
                    sharingStatus = uiState.sharingStatus,
                    lastUpdateTime = uiState.lastUpdateTime
                )
            }
        }
    }

    // Diálogo de selección de rutas
    RouteSelectionDialog(
        showDialog = showRouteDialog,
        onDismiss = { showRouteDialog = false },
        onRouteSelected = { route ->
            viewModel.onRouteSelected(route)
            showRouteDialog = false
        },
        selectedRoute = uiState.selectedRoute,
        repository = repository
    )
}

@Composable
fun ConnectionStatusCard(
    isNetworkAvailable: Boolean,
    sharingStatus: SharingStatus
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isNetworkAvailable -> MaterialTheme.colorScheme.errorContainer
                sharingStatus == SharingStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when {
                    !isNetworkAvailable -> Icons.Default.Warning
                    sharingStatus == SharingStatus.ERROR -> Icons.Default.Error
                    else -> Icons.Default.CheckCircle
                },
                contentDescription = "Estado de conexión",
                tint = when {
                    !isNetworkAvailable -> MaterialTheme.colorScheme.error
                    sharingStatus == SharingStatus.ERROR -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            Text(
                text = when {
                    !isNetworkAvailable -> "Sin conexión a internet"
                    sharingStatus == SharingStatus.ERROR -> "Error de conexión"
                    else -> "Conectado"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    !isNetworkAvailable -> MaterialTheme.colorScheme.onErrorContainer
                    sharingStatus == SharingStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
fun TruckInfoCard(
    truckId: String,
    currentLocation: com.google.android.gms.maps.model.LatLng?,
    lastUpdateTime: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Información del Camión",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "ID del Camión: $truckId",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (currentLocation != null) {
                Text(
                    text = "Ubicación actual:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Lat: ${String.format("%.6f", currentLocation.latitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Lng: ${String.format("%.6f", currentLocation.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (lastUpdateTime > 0) {
                    val timeAgo = (System.currentTimeMillis() - lastUpdateTime) / 1000
                    Text(
                        text = "Última actualización: hace ${timeAgo}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Obteniendo ubicación...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RouteSelectionCard(
    selectedRoute: Route?,
    onRouteSelected: (Route) -> Unit,
    onShowRouteDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Seleccionar Ruta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (selectedRoute != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = selectedRoute.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Horario: ${selectedRoute.operatingHours}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Text(
                    text = "Selecciona una ruta para compartir la ubicación",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Botón para mostrar el diálogo de selección de rutas
            Button(
                onClick = onShowRouteDialog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Route,
                    contentDescription = "Seleccionar ruta",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedRoute == null) "Seleccionar Ruta" else "Cambiar Ruta")
            }
        }
    }
}

@Composable
fun SharingControlsCard(
    isSharing: Boolean,
    isLocationPermissionGranted: Boolean,
    selectedRoute: Route?,
    sharingStatus: SharingStatus,
    onStartSharing: () -> Unit,
    onStopSharing: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Controles de Compartir",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isSharing) {
                    Button(
                        onClick = onStartSharing,
                        enabled = isLocationPermissionGranted && selectedRoute != null,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Iniciar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Compartir")
                    }
                } else {
                    Button(
                        onClick = onStopSharing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Detener",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detener Compartir")
                    }
                }
            }
            
            if (!isLocationPermissionGranted) {
                Text(
                    text = "Se requieren permisos de ubicación",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            if (selectedRoute == null) {
                Text(
                    text = "Selecciona una ruta antes de compartir",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SharingStatusCard(
    sharingStatus: SharingStatus,
    lastUpdateTime: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (sharingStatus) {
                SharingStatus.SHARING -> MaterialTheme.colorScheme.primaryContainer
                SharingStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (sharingStatus) {
                SharingStatus.STARTING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                SharingStatus.SHARING -> {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Compartiendo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                SharingStatus.ERROR -> {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pausado",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column {
                Text(
                    text = when (sharingStatus) {
                        SharingStatus.STARTING -> "Iniciando compartir..."
                        SharingStatus.SHARING -> "Compartiendo ubicación en tiempo real"
                        SharingStatus.ERROR -> "Error al compartir ubicación"
                        else -> "Estado: $sharingStatus"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (lastUpdateTime > 0) {
                    val timeAgo = (System.currentTimeMillis() - lastUpdateTime) / 1000
                    Text(
                        text = "Última actualización: hace ${timeAgo}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
