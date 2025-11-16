package com.example.mirutadigital.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una ruta compartida en el historial
 *
 * @param id ID unico del registro de historial
 * @param routeId ID de la ruta (ej: "R_17")
 * @param routeName Nombre de la ruta para mostrar
 * @param sharedAt Marca de tiempo cuando se compartio
 */
@Entity(tableName = "route_history")
data class RouteHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: String,
    val routeName: String,
    val sharedAt: Long = System.currentTimeMillis()
)
