package com.example.mirutadigital.data.model.ui

import com.example.mirutadigital.data.model.ui.base.Stop

data class JourneyInfo(
    val stops: List<Stop>,
    val encodedPolyline: String? = null
)

// modelo para simplificar lo que se manda a la ui para mostrar las rutas
data class RoutesInfo(
    val id: String,
    val name: String,
    val windshieldLabel: String,
    val colors: String,
    val stopsJourney: List<JourneyInfo>,
)
