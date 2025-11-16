package com.example.mirutadigital.data.local.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.mirutadigital.data.local.entities.JourneyEntity
import com.example.mirutadigital.data.local.entities.JourneyStopCrossRef
import com.example.mirutadigital.data.local.entities.StopEntity

/**
 * POJO de relacion: Una parada con todos los trayectos que pasan por ella,
 * trayectos de diferentes rutas
 */
data class StopWithJourneys(
    @Embedded
    val stop: StopEntity,

    @Relation(
        parentColumn = "stopId",
        entityColumn = "journeyId",
        associateBy = Junction(
            value = JourneyStopCrossRef::class,
            parentColumn = "stopId",
            entityColumn = "journeyId"
        )
    )
    val journeys: List<JourneyEntity>
)
