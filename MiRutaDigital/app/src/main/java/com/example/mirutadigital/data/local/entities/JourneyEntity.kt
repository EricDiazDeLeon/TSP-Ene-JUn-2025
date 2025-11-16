package com.example.mirutadigital.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un trayecto (ida o vuelta) de una ruta
 *
 * @param journeyId ID unico (ej: "R_17_OUTBOUND")
 * @param routeOwnerId Clave foranea que lo relaciona a RouteEntity (ej: "R_17")
 * @param journeyType Un tipo para distinguirlo (ej: "OUTBOUND" o "INBOUND")
 * @param firstDeparture Hora de primera salida (ej: "06:30")
 * @param lastDeparture Hora de ultima salida (ej: "21:00")
 * @param encodedPolyline Polilinea para el mapa codificada a ascci
 **/
@Entity(
    tableName = "journeys",
    foreignKeys =  [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["routeId"],
            childColumns = ["routeOwnerId"],
            onDelete = ForeignKey.CASCADE // si se borra, se borran sus referencias
        )
    ],
    indices = [Index("routeOwnerId")]
)
data class JourneyEntity(
    @PrimaryKey val journeyId: String,
    val routeOwnerId: String,
    val journeyType: String,
    val firstDeparture: String,
    val lastDeparture: String,
    val encodedPolyline: String?
)