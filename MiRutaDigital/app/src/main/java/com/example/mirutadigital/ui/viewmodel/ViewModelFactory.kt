package com.example.mirutadigital.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mirutadigital.data.repository.AppRepository

class ViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(RoutesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoutesListViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(SharingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
