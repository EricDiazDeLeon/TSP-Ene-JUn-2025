package com.example.mirutadigital.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.repository.AppRepository
import kotlinx.coroutines.launch

class RoutesListViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _availableRoutes = MutableLiveData<List<Route>>()
    val availableRoutes: LiveData<List<Route>> = _availableRoutes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadAvailableRoutes()
    }

    private fun loadAvailableRoutes() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAvailableRoutes().collect { routesList ->
                _availableRoutes.value = routesList
                _isLoading.value = false
            }
        }
    }
}
