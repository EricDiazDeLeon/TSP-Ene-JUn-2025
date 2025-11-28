package com.example.mirutadigital.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_ratings")
data class RouteRatingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: String,
    val userId: String,
    val stars: Int,
    val comment: String?,
    val createdAt: Long,
    val pendingSync: Boolean = true
)
