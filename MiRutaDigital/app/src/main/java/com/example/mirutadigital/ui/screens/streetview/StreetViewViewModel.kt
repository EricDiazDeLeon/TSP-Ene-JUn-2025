package com.example.mirutadigital.ui.screens.streetview

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.ui.util.SnackbarManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StreetViewUiState(
    val lat: Double? = null,
    val lng: Double? = null,
    val isLoading: Boolean = true,
    val hasCoverageError: Boolean = false,
    val hasInternetError: Boolean = false
)

class StreetViewViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(StreetViewUiState())
    val uiState: StateFlow<StreetViewUiState> = _uiState.asStateFlow()

    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _uiState.update { it.copy(hasInternetError = false) }
        }

        override fun onLost(network: Network) {
            _uiState.update { it.copy(hasInternetError = true) }
            showMessage("Se ha perdido la conexión a internet.")
        }
    }

    fun initialize(coords: String) {
        if (_uiState.value.lat != null) return // Ya inicializado

        startNetworkMonitoring()
        checkConnectivity()
        parseCoordinates(coords)
    }

    private fun startNetworkMonitoring() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkConnectivity() {
        val connected = isNetworkAvailable(getApplication())
        if (!connected) {
            _uiState.update { it.copy(hasInternetError = true) }
            showMessage("Se requiere internet para cargar la vista calle.")
        } else {
            _uiState.update { it.copy(hasInternetError = false) }
        }
    }

    private fun parseCoordinates(coords: String) {
        val parts = coords.split(",")
        val lat = parts.getOrNull(0)?.toDoubleOrNull()
        val lng = parts.getOrNull(1)?.toDoubleOrNull()
        
        _uiState.update { it.copy(lat = lat, lng = lng) }
    }

    fun onPanoramaLoaded() {
        _uiState.update { it.copy(isLoading = false, hasCoverageError = false) }
    }

    fun onPanoramaFailed() {
        _uiState.update { it.copy(isLoading = false, hasCoverageError = true) }
        showMessage("La vista a nivel de calle no está disponible para esta ubicación.")
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            SnackbarManager.showMessage(message)
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

    override fun onCleared() {
        super.onCleared()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Already unregistered or not registered
        }
    }
}
