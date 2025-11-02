package com.example.mirutadigital.data.model

import com.google.android.gms.maps.model.LatLng

data class Route(
    val id: String,
    val name: String,
    val operatingHours: String,
    val polylinePoints: List<LatLng>,
    val isFavorite: Boolean = false
)
