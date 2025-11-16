package com.example.mirutadigital.data.local.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.mirutadigital.data.local.entities.JourneyEntity
import com.example.mirutadigital.data.local.entities.JourneyStopCrossRef
import com.example.mirutadigital.data.local.entities.StopEntity

/**
 * POJO de relacion: Un trayecto con su lista ordenada de paradas
 */
data class JourneyWithStops(
    @Embedded
    val journey: JourneyEntity,

    @Relation(
        parentColumn = "journeyId",
        entityColumn = "stopId",
        associateBy = Junction(
            value = JourneyStopCrossRef::class,
            parentColumn = "journeyId",
            entityColumn = "stopId"
        )
    )
    val stops: List<StopEntity>
)
