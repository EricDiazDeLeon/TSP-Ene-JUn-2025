package com.example.mirutadigital.data.model.ui

// el modelos y logica que necesita la interfaz para mostrar
data class StopWithRoutes(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val routes: List<RouteInfo>,
    val distance: String = "Calculando..."
)

// modelo de resumen de ruta para mostrarlo en la lista
data class RouteInfo(
    val name: String,
    val destination: String
)
