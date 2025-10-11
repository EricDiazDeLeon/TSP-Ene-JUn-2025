package com.example.mirutadigital.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.model.LiveTruck
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.model.Stop
import com.example.mirutadigital.data.repository.AppRepository
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _routes = MutableLiveData<List<Route>>()
    val routes: LiveData<List<Route>> = _routes

    private val _stops = MutableLiveData<List<Stop>>()
    val stops: LiveData<List<Stop>> = _stops

    private val _liveTrucks = MutableLiveData<List<LiveTruck>>()
    val liveTrucks: LiveData<List<LiveTruck>> = _liveTrucks

    private val _navigateToRoutesList = MutableLiveData<Boolean>()
    val navigateToRoutesList: LiveData<Boolean> = _navigateToRoutesList

    private val _navigateToSharing = MutableLiveData<Boolean>()
    val navigateToSharing: LiveData<Boolean> = _navigateToSharing

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getRoutes().collect { routesList ->
                _routes.value = routesList
            }
        }

        viewModelScope.launch {
            repository.getStops().collect { stopsList ->
                _stops.value = stopsList
            }
        }

        viewModelScope.launch {
            repository.getLiveTrucks().collect { trucksList ->
                _liveTrucks.value = trucksList
            }
        }
    }

    fun onShowRoutesListClicked() {
        _navigateToRoutesList.value = true
    }

    fun onShareLocationClicked() {
        _navigateToSharing.value = true
    }

    fun onNavigationHandled() {
        _navigateToRoutesList.value = false
        _navigateToSharing.value = false
    }
}
