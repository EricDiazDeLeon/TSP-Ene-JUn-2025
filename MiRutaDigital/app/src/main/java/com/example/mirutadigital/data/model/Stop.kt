package com.example.mirutadigital.data.model

import com.google.android.gms.maps.model.LatLng

data class Stop(
    val id: String,
    val name: String,
    val location: LatLng,
    val associatedRouteIds: List<String> = emptyList()
)
