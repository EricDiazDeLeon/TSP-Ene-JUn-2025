package com.example.mirutadigital.data.testsUi.model

import com.google.android.gms.maps.model.LatLng

// Modelo solo para probar la vista

/**
 * Modelo Parada de autobus
 * @param id no se si lo tengan como string
 * @param coordinates la latitud y longitud pero con el tipo de dato que usa google
 */
data class Stop(
    val id: String,
    val name: String,
    val coordinates: LatLng
)
