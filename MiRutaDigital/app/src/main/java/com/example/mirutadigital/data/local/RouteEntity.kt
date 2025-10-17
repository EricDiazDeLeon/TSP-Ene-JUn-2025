package com.example.mirutadigital.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val operatingHours: String,
    val polylinePointsJson: String
)
