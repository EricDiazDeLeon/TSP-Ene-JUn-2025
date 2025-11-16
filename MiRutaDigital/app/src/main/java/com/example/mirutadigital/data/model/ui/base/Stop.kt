package com.example.mirutadigital.data.model.ui.base

import com.google.android.gms.maps.model.LatLng

/**
 * Modelo Parada de autobus
 * @param id no se si lo tengan como string
 * @param coordinates la latitud y longitud pero con el tipo de dato que usa google
 */
data class Stop(
    val id: String,
    val name: String,
    val coordinates: LatLng // hay una funcion que convierte lo dos double en latLng(1.5,2.3)
)