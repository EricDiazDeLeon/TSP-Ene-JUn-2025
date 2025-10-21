package com.example.mirutadigital.ui.screens.shareLocation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.repository.AppRepository
import com.example.mirutadigital.data.service.LocationService
import com.example.mirutadigital.data.service.NetworkMonitorService
import com.example.mirutadigital.data.service.SharingStateManager
import com.example.mirutadigital.data.service.SharingState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShareLocationUiState(
    val isSharing: Boolean = false,
    val currentLocation: com.google.android.gms.maps.model.LatLng? = null,
    val selectedRoute: Route? = null,
    val truckId: String = "",
    val isLocationPermissionGranted: Boolean = false,
    val isNetworkAvailable: Boolean = true,
    val errorMessage: String? = null,
    val sharingStatus: com.example.mirutadigital.data.service.SharingStatus = com.example.mirutadigital.data.service.SharingStatus.STOPPED,
    val lastUpdateTime: Long = 0L
)

class ShareLocationViewModel(
    private val repository: AppRepository,
    private val sharingStateManager: SharingStateManager,
    private val locationService: LocationService
) : ViewModel() {

    val uiState: StateFlow<ShareLocationUiState> = sharingStateManager.sharingState.map { sharingState ->
        ShareLocationUiState(
            isSharing = sharingState.isSharing,
            currentLocation = sharingState.currentLocation,
            selectedRoute = sharingState.selectedRoute,
            truckId = sharingState.truckId,
            isLocationPermissionGranted = sharingState.isLocationPermissionGranted,
            isNetworkAvailable = sharingState.isNetworkAvailable,
            errorMessage = sharingState.errorMessage,
            sharingStatus = sharingState.sharingStatus,
            lastUpdateTime = sharingState.lastUpdateTime
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = ShareLocationUiState()
    )

    init {
        // Verificar permisos de ubicación al inicializar
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        // Verificar permisos usando el LocationService directamente
        val hasPermission = locationService.hasLocationPermission()
        sharingStateManager.updateLocationPermission(hasPermission)
    }

    fun onLocationPermissionGranted() {
        sharingStateManager.updateLocationPermission(true)
    }

    fun onRouteSelected(route: Route) {
        // El estado de la ruta seleccionada se maneja localmente en la pantalla
        // ya que el SharingStateManager solo maneja el estado cuando se está compartiendo
    }

    fun startSharing(selectedRoute: Route) {
        sharingStateManager.startSharing(selectedRoute)
    }

    fun stopSharing() {
        sharingStateManager.stopSharing()
    }

    fun clearError() {
        sharingStateManager.clearError()
    }

    override fun onCleared() {
        super.onCleared()
        // No destruimos el SharingStateManager aquí porque es global
        // Solo limpiamos errores si es necesario
    }
}
