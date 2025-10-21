package com.example.mirutadigital.ui.screens.shareLocation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.remote.FirestoreService
import com.example.mirutadigital.data.repository.AppRepository
import com.example.mirutadigital.data.service.LocationService
import com.example.mirutadigital.data.service.NetworkMonitorService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

data class ShareLocationUiState(
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

class ShareLocationViewModel(
    private val repository: AppRepository,
    private val locationService: LocationService,
    private val networkMonitorService: NetworkMonitorService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareLocationUiState())
    val uiState: StateFlow<ShareLocationUiState> = _uiState.asStateFlow()

    private var locationUpdatesJob: Job? = null
    private var sharingJob: Job? = null
    private var networkMonitoringJob: Job? = null
    private val firestoreService = FirestoreService()

    companion object {
        private const val TAG = "ShareLocationViewModel"
        private const val SHARING_INTERVAL = 10000L // 10 segundos
    }

    init {
        // Generar un ID único para el camión
        _uiState.value = _uiState.value.copy(truckId = generateTruckId())
        
        // Verificar permisos de ubicación
        checkLocationPermission()
        
        // Iniciar monitoreo de red
        startNetworkMonitoring()
    }

    private fun generateTruckId(): String {
        return "truck_${UUID.randomUUID().toString().substring(0, 8)}"
    }

    private fun checkLocationPermission() {
        val hasPermission = locationService.hasLocationPermission()
        _uiState.value = _uiState.value.copy(isLocationPermissionGranted = hasPermission)
        
        if (!hasPermission) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Se requieren permisos de ubicación para compartir la posición del camión"
            )
        }
    }

    private fun startNetworkMonitoring() {
        networkMonitoringJob = viewModelScope.launch {
            networkMonitorService.getNetworkStatus()
                .collect { isConnected ->
                    val wasConnected = _uiState.value.isNetworkAvailable
                    _uiState.value = _uiState.value.copy(isNetworkAvailable = isConnected)
                    
                    if (!wasConnected && isConnected) {
                        // Red reconectada
                        onNetworkReconnected()
                    } else if (wasConnected && !isConnected) {
                        // Red perdida
                        _uiState.value = _uiState.value.copy(
                            sharingStatus = SharingStatus.PAUSED,
                            errorMessage = "Conexión perdida. El compartir se pausará hasta reconectar."
                        )
                    }
                }
        }
    }

    fun onLocationPermissionGranted() {
        _uiState.value = _uiState.value.copy(
            isLocationPermissionGranted = true,
            errorMessage = null
        )
    }

    fun onRouteSelected(route: Route) {
        _uiState.value = _uiState.value.copy(selectedRoute = route)
    }

    fun startSharing() {
        if (!_uiState.value.isLocationPermissionGranted) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Se requieren permisos de ubicación"
            )
            return
        }

        if (_uiState.value.selectedRoute == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Selecciona una ruta antes de compartir la ubicación"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isSharing = true,
            sharingStatus = SharingStatus.STARTING,
            errorMessage = null
        )

        // Iniciar seguimiento de ubicación
        startLocationTracking()
    }

    fun stopSharing() {
        _uiState.value = _uiState.value.copy(
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
        locationUpdatesJob = viewModelScope.launch {
            locationService.startLocationUpdates()
                .catch { exception ->
                    Log.e(TAG, "Error en seguimiento de ubicación", exception)
                    _uiState.value = _uiState.value.copy(
                        sharingStatus = SharingStatus.ERROR,
                        errorMessage = "Error al obtener ubicación: ${exception.message}"
                    )
                }
                .collect { location ->
                    val newLocation = LatLng(location.latitude, location.longitude)
                    _uiState.value = _uiState.value.copy(
                        currentLocation = newLocation,
                        lastUpdateTime = System.currentTimeMillis()
                    )

                    // Si estamos compartiendo, enviar la ubicación
                    if (_uiState.value.isSharing) {
                        shareCurrentLocation(newLocation)
                    }
                }
        }
    }

    private fun shareCurrentLocation(location: LatLng) {
        sharingJob?.cancel()
        sharingJob = viewModelScope.launch {
            try {
                val routeId = _uiState.value.selectedRoute?.id ?: return@launch
                
                firestoreService.shareTruckLocation(
                    truckId = _uiState.value.truckId,
                    routeId = routeId,
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                _uiState.value = _uiState.value.copy(
                    sharingStatus = SharingStatus.SHARING,
                    errorMessage = null
                )

                Log.d(TAG, "Ubicación compartida: ${location.latitude}, ${location.longitude}")
                
                // Programar la siguiente actualización
                delay(SHARING_INTERVAL)
                
            } catch (exception: Exception) {
                Log.e(TAG, "Error al compartir ubicación", exception)
                _uiState.value = _uiState.value.copy(
                    sharingStatus = SharingStatus.ERROR,
                    errorMessage = "Error de conexión: ${exception.message}",
                    isNetworkAvailable = false
                )
                
                // Intentar reconectar después de un delay
                delay(5000)
                if (_uiState.value.isSharing) {
                    shareCurrentLocation(location)
                }
            }
        }
    }

    fun onNetworkReconnected() {
        _uiState.value = _uiState.value.copy(
            isNetworkAvailable = true,
            errorMessage = null
        )
        
        // Si estábamos compartiendo, reanudar
        if (_uiState.value.isSharing && _uiState.value.currentLocation != null) {
            _uiState.value = _uiState.value.copy(sharingStatus = SharingStatus.SHARING)
            shareCurrentLocation(_uiState.value.currentLocation!!)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopSharing()
        networkMonitoringJob?.cancel()
    }
}
