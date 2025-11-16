package com.example.mirutadigital.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)
    private val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    private val _isGpsEnabled = MutableStateFlow(isGpsProviderEnabled())
    val isGpsEnabled = _isGpsEnabled.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                    _location.value = it
            }
        }
    }

    private val gpsStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                _isGpsEnabled.value = isGpsProviderEnabled()
            }
        }
    }

    init {
        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        application.registerReceiver(gpsStateReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)

        isGpsEnabled
            .onEach { enabled ->
                if (enabled) {
                    startLocationUpdates()
                } else {
                    stopLocationUpdates()
                    _location.value = null
                }
            }
            .launchIn(viewModelScope)
    }

    private fun isGpsProviderEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!isGpsProviderEnabled()) return

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdateDelayMillis(15000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        getApplication<Application>().unregisterReceiver(gpsStateReceiver)
    }
}
