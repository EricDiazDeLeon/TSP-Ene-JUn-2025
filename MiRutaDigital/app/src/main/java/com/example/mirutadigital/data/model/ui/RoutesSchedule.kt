package com.example.mirutadigital.data.model.ui

// modelo para simplificar lo que se manda a la ui para mostrar el horario de las rutas
data class RouteInfoSchedulel(
    val id: String,
    val name: String,
    val outboundInfo: Schedule,
    val inboundInfo: Schedule
)

data class Schedule(
    val nameOrigin: String,
    val nameDestination: String,
    val schedule: Pair<String, String>, // hh:mm - hh:mm
)

