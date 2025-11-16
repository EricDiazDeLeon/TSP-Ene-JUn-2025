package com.example.mirutadigital.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una parada en la base de datos
 *
 * @param stopId ID unico (ej: "plaza_alesia"
 * @param name Nombre (ej: "Plaza Alesia")
 * @param latitude Latitud de la coordenada
 * @param longitude Longitud de la coordenada
 */
@Entity(tableName = "stops")
data class StopEntity(
    @PrimaryKey val stopId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)
