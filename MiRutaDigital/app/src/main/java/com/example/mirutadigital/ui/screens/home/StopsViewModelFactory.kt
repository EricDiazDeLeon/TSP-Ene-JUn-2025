package com.example.mirutadigital.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mirutadigital.data.repository.AppRepository

class StopsViewModelFactory(
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StopsViewModel::class.java)) {
            return StopsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}