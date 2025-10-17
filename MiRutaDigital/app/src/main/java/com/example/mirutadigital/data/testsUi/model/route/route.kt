package com.example.mirutadigital.data.testsUi.model.route

/**
 * Modelo Ruta : toda la info de la ruta
 *
 * @param id identificador único de la ruta tal vez "R_17" o no se si un Int autoincremental este mejor,
 *           segun yo como son pocas es mejor un string representativo
 * @param name nombre o código de la ruta algo como "Ruta 17" o "Pericos"
 * @param price costo del pasaje
 * @param departureInterval intervalos, cada x minutos sale por ejemplo la ruta 17 cada 8, en minutos, departureInterval = 8
 * @param outboundJourney trayecto de ida
 * @param inboundJourney trayecto de vuelta
 */
data class Route(
    val id: String,
    val name: String,
    val price: Double,
    val departureInterval: Int,
    val outboundJourney: Journey,
    val inboundJourney: Journey,
)
