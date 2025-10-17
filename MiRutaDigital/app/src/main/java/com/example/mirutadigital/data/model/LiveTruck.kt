package com.example.mirutadigital.data.model

import com.google.android.gms.maps.model.LatLng

data class LiveTruck(
    val truckId: String,
    val routeId: String,
    val location: LatLng,
    val lastUpdate: Long,
    val viewersCount: Int
)
