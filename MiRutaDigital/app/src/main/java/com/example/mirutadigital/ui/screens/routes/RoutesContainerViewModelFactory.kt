package com.example.mirutadigital.ui.screens.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mirutadigital.data.repository.AppRepository

class RoutesContainerViewModelFactory(
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutesContainerViewModel::class.java)) {
            return RoutesContainerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
