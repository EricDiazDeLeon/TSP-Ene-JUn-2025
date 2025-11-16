package com.example.mirutadigital.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.mirutadigital.data.local.entities.JourneyEntity
import com.example.mirutadigital.data.local.entities.RouteEntity

/**
 * POJO de relacion: Una ruta completa con sus trayectos, y las paradas de esos trayectos
 */
data class RouteWithJourneys(
    @Embedded
    val route: RouteEntity,

    @Relation(
        // Relacion anidada Route -> Journey -> Stops
        entity = JourneyEntity::class,
        parentColumn = "routeId",
        entityColumn = "routeOwnerId"
    )
    val journeys: List<JourneyWithStops>
)
