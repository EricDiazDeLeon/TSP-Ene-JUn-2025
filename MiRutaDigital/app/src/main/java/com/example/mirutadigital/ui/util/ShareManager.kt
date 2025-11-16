package com.example.mirutadigital.ui.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ShareManager {
    private val _currentSharedRouteId = MutableStateFlow<String?>(null)
    val currentSharedRouteId = _currentSharedRouteId.asStateFlow()

    fun startSharing(routeId: String) {
        _currentSharedRouteId.value = routeId
    }

    fun stopSharing() {
        _currentSharedRouteId.value = null
    }
}
