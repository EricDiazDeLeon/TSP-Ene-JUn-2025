package com.example.mirutadigital.ui.screens.shareLocation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mirutadigital.data.repository.AppRepository
import com.example.mirutadigital.data.service.LocationService
import com.example.mirutadigital.data.service.NetworkMonitorService

class ShareLocationViewModelFactory(
    private val repository: AppRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareLocationViewModel::class.java)) {
            val locationService = LocationService(context)
            val networkMonitorService = NetworkMonitorService(context)
            return ShareLocationViewModel(repository, locationService, networkMonitorService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
