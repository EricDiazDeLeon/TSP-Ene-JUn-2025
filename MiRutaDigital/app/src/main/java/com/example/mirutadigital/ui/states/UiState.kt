package com.example.mirutadigital.ui.states

/**
 * Sealed class para manejar diferentes estados de UI de manera consistente
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

/**
 * Estados específicos para operaciones de red
 */
sealed class NetworkState {
    object Idle : NetworkState()
    object Loading : NetworkState()
    object Success : NetworkState()
    data class Error(val message: String) : NetworkState()
}

/**
 * Estados para localización
 */
sealed class LocationState {
    object Unknown : LocationState()
    object Disabled : LocationState()
    object PermissionDenied : LocationState()
    data class Available(val latitude: Double, val longitude: Double) : LocationState()
}