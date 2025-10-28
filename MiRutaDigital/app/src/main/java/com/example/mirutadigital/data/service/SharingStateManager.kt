package com.example.mirutadigital.data.service

import android.content.Context
import android.util.Log
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.remote.FirestoreService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Gestor de estado global para el compartir ubicación.
 * Mantiene el estado persistente entre navegaciones de pantalla (segundo plano).
 */
class SharingStateManager(
    private val context: Context,
    private val locationService: LocationService,
    private val networkMonitorService: NetworkMonitorService
) {
    
    private val _sharingState = MutableStateFlow(SharingState())
    val sharingState: StateFlow<SharingState> = _sharingState.asStateFlow()
    
    private val firestoreService = FirestoreService()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var locationUpdatesJob: Job? = null
    private var sharingJob: Job? = null
    private var networkMonitoringJob: Job? = null
    
    companion object {
        private const val TAG = "SharingStateManager"
        private const val SHARING_INTERVAL = 10000L // 10 segundos
        
        @Volatile
        private var INSTANCE: SharingStateManager? = null
        
        fun getInstance(
            context: Context,
            locationService: LocationService,
            networkMonitorService: NetworkMonitorService
        ): SharingStateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharingStateManager(context, locationService, networkMonitorService)
                    .also { INSTANCE = it }
            }
        }
    }
    
    init {
        // Generar un ID único para el camión si no existe
        if (_sharingState.value.truckId.isEmpty()) {
            _sharingState.value = _sharingState.value.copy(truckId = generateTruckId())
        }
        
        // Iniciar monitoreo de red
        startNetworkMonitoring()
    }
    
    private fun generateTruckId(): String {
        return "truck_${UUID.randomUUID().toString().substring(0, 8)}"
    }
    
    private fun startNetworkMonitoring() {
        networkMonitoringJob = coroutineScope.launch {
            networkMonitorService.getNetworkStatus()
                .collect { isConnected ->
                    val wasConnected = _sharingState.value.isNetworkAvailable
                    _sharingState.value = _sharingState.value.copy(isNetworkAvailable = isConnected)
                    
                    if (!wasConnected && isConnected) {
                        // Red reconectada
                        onNetworkReconnected()
                    } else if (wasConnected && !isConnected) {
                        // Red perdida
                        _sharingState.value = _sharingState.value.copy(
                            sharingStatus = SharingStatus.PAUSED,
                            errorMessage = "Conexión perdida. El compartir se pausará hasta reconectar."
                        )
                    }
                }
        }
    }
    
    fun startSharing(selectedRoute: Route) {
        if (!locationService.hasLocationPermission()) {
            _sharingState.value = _sharingState.value.copy(
                errorMessage = "Se requieren permisos de ubicación"
            )
            return
        }
        
        _sharingState.value = _sharingState.value.copy(
            isSharing = true,
            selectedRoute = selectedRoute,
            sharingStatus = SharingStatus.STARTING,
            errorMessage = null
        )
        
        // Iniciar seguimiento de ubicación
        startLocationTracking()
        
        Log.d(TAG, "Iniciando compartir ubicación para ruta: ${selectedRoute.name}")
    }
    
    fun stopSharing() {
        _sharingState.value = _sharingState.value.copy(
            isSharing = false,
            sharingStatus = SharingStatus.STOPPED,
            errorMessage = null
        )
        
        // Detener trabajos
        locationUpdatesJob?.cancel()
        sharingJob?.cancel()
        locationService.stopLocationUpdates()
        
        Log.d(TAG, "Compartir ubicación detenido")
    }
    
    private fun startLocationTracking() {
        locationUpdatesJob = coroutineScope.launch {
            locationService.startLocationUpdates()
                .catch { exception ->
                    // Ignorar cancelaciones provocadas al detener el compartir
                    if (exception is CancellationException) {
                        Log.d(TAG, "Seguimiento de ubicación cancelado")
                        return@catch
                    }
                    Log.e(TAG, "Error en seguimiento de ubicación", exception)
                    _sharingState.value = _sharingState.value.copy(
                        sharingStatus = SharingStatus.ERROR,
                        errorMessage = "Error al obtener ubicación: ${exception.message}"
                    )
                }
                .collect { location ->
                    val newLocation = LatLng(location.latitude, location.longitude)
                    _sharingState.value = _sharingState.value.copy(
                        currentLocation = newLocation,
                        lastUpdateTime = System.currentTimeMillis()
                    )
                    
                    // Si estamos compartiendo, enviar la ubicación
                    if (_sharingState.value.isSharing) {
                        shareCurrentLocation(newLocation)
                    }
                }
        }
    }
    
    private fun shareCurrentLocation(location: LatLng) {
        sharingJob?.cancel()
        sharingJob = coroutineScope.launch {
            try {
                val routeId = _sharingState.value.selectedRoute?.id ?: return@launch
                
                firestoreService.shareTruckLocation(
                    truckId = _sharingState.value.truckId,
                    routeId = routeId,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                
                _sharingState.value = _sharingState.value.copy(
                    sharingStatus = SharingStatus.SHARING,
                    errorMessage = null
                )
                
                Log.d(TAG, "Ubicación compartida: ${location.latitude}, ${location.longitude}")
                
                // Programar la siguiente actualización
                delay(SHARING_INTERVAL)
                
            } catch (exception: Exception) {
                // Si la cancelación fue intencional (al detener compartir), no lo tratamos como error
                if (exception is CancellationException) {
                    Log.d(TAG, "Compartir ubicación cancelado")
                    return@launch
                }
                Log.e(TAG, "Error al compartir ubicación", exception)
                _sharingState.value = _sharingState.value.copy(
                    sharingStatus = SharingStatus.ERROR,
                    errorMessage = "Error de conexión: ${exception.message}",
                )
                
                // Intentar reconectar después de un delay
                delay(5000)
                if (_sharingState.value.isSharing) {
                    shareCurrentLocation(location)
                }
            }
        }
    }
    
    fun onNetworkReconnected() {
        _sharingState.value = _sharingState.value.copy(
            isNetworkAvailable = true,
            errorMessage = null
        )
        
        // Si estábamos compartiendo, reanudar
        if (_sharingState.value.isSharing && _sharingState.value.currentLocation != null) {
            _sharingState.value = _sharingState.value.copy(sharingStatus = SharingStatus.SHARING)
            shareCurrentLocation(_sharingState.value.currentLocation!!)
        }
    }
    
    fun clearError() {
        _sharingState.value = _sharingState.value.copy(errorMessage = null)
    }
    
    fun updateLocationPermission(granted: Boolean) {
        _sharingState.value = _sharingState.value.copy(isLocationPermissionGranted = granted)
    }
    
    fun destroy() {
        stopSharing()
        networkMonitoringJob?.cancel()
        coroutineScope.cancel()
    }
}

data class SharingState(
    val isSharing: Boolean = false,
    val currentLocation: LatLng? = null,
    val selectedRoute: Route? = null,
    val truckId: String = "",
    val isLocationPermissionGranted: Boolean = false,
    val isNetworkAvailable: Boolean = true,
    val errorMessage: String? = null,
    val sharingStatus: SharingStatus = SharingStatus.STOPPED,
    val lastUpdateTime: Long = 0L
)

enum class SharingStatus {
    STOPPED,
    STARTING,
    SHARING,
    PAUSED,
    ERROR
}
