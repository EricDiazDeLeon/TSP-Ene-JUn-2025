package com.example.mirutadigital.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una ruta marcada como favorita por el usuario
 *
 * @param routeId ID de la ruta favorita (ej: "R_17")
 * @param addedAt Marca de tiempo cuando se agrego a favoritos
 */
@Entity(tableName = "favorite_routes")
data class FavoriteRouteEntity(
    @PrimaryKey val routeId: String,
    val addedAt: Long = System.currentTimeMillis()
)
