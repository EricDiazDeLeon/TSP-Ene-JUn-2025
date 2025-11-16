package com.example.mirutadigital.data.model.ui.base.route

/**
 * Modelo RouteDetail : info adicional de la ruta
 *
 * @param windshieldLabel es las frases que tienen en los parabrisas las rutas
 * @param colors el color del camion (Amarillo - Verde)
 * @param price costo del pasaje
 */
data class RouteDetail(
    val windshieldLabel: String,
    val colors: String,
    val price: Double
)
