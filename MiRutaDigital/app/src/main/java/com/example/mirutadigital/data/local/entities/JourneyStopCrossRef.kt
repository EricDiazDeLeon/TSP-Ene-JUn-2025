package com.example.mirutadigital.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Tabla de referencia cruzada (Junction) para crear la relacion M-M
 * entre las entidades Journey y Stop
 *
 * se define la tabla de la lista ordenada de paradas para cada trayecto
 *
 * @param jouneyId ID del trayecto (ej: "R_17_OUTBOUND")
 * @param stopId ID de la parada (ej: "plaza_alesia")
 * @param stopOrder el orden de la parada en el trayecto (0, 1, 2 ...)
 */
@Entity(
    tableName = "journey_stop_cross_ref",
    primaryKeys = ["journeyId", "stopId"], // clave primaria compuesta
    indices = [Index("journeyId"), Index("stopId")],
    foreignKeys = [
        ForeignKey(
            entity = JourneyEntity::class,
            parentColumns = ["journeyId"],
            childColumns = ["journeyId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StopEntity::class,
            parentColumns = ["stopId"],
            childColumns = ["stopId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class JourneyStopCrossRef(
    val journeyId: String,
    val stopId: String,
    val stopOrder: Int
)
