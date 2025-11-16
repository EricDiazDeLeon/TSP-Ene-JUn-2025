package com.example.mirutadigital.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una ruta de autobus y sus detalles
 * Los trayectos (JourneyEntity) se relacionn a esta ruta
 *
 * @param routeId ID unico (ej: "R_17")
 * @param name Nombre (ej: "Ruta 17")
 * @param departureInterval Frecuencia de salida (ej: 10), en minutos
 *
 * @param windshieldLabel Texto del parabrisas
 * @param colors Colores de la ruta
 * @param price Precio de la ruta
 *
 * @param lastUpdate Marca de tiempo para saber si esta desactualizada
 */
@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val routeId: String,
    val name: String,
    val departureInterval: Int,

    val windshieldLabel: String,
    val colors: String,
    val price: Double,

    // control de sincronizacion
    val lastUpdate: Long? = null
)
