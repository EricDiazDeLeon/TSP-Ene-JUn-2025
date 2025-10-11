package com.example.mirutadigital.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.data.model.LiveTruck
import com.example.mirutadigital.data.repository.AppRepository
import kotlinx.coroutines.launch

class SharingViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _sharingStatus = MutableLiveData<String>()
    val sharingStatus: LiveData<String> = _sharingStatus

    private val _viewersCount = MutableLiveData<Int>()
    val viewersCount: LiveData<Int> = _viewersCount

    private val _isSharing = MutableLiveData<Boolean>()
    val isSharing: LiveData<Boolean> = _isSharing

    init {
        _sharingStatus.value = "Iniciando..."
        _viewersCount.value = 0
        _isSharing.value = false
    }

    fun startListening(truckId: String) {
        viewModelScope.launch {
            repository.getLiveTruckDetails(truckId).collect { truck ->
                truck?.let {
                    _viewersCount.value = it.viewersCount
                    _isSharing.value = true
                    _sharingStatus.value = "Compartiendo ubicación"
                } ?: run {
                    _isSharing.value = false
                    _sharingStatus.value = "Error al obtener datos del camión"
                }
            }
        }
    }

    fun stopSharing() {
        _isSharing.value = false
        _sharingStatus.value = "Compartir detenido"
    }
}
