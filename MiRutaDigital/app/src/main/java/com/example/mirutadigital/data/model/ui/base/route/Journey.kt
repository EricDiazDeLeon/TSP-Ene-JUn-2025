package com.example.mirutadigital.data.model.ui.base.route

import com.example.mirutadigital.data.model.ui.base.Stop


/**
 * Modelo Trayecto de una ruta: el de ida y vuelta de una ruta
 *
 * @param stops la lista ordenada de paradas de la ruta
 *              el orden en de la lista es el trayecto osea RUTA 17 -> ("UAZ", "GALERIAS", ... , "VILLAS DE GUADALUPE").
 * @param firstDeparture hora de la primera salida con formato "HH:MM am/pm" am o pm
 * @param lastDeparture hora de la ultima salida formato "HH:MM am/pm".
 * @param encodedPolyline polilinea codificada, para dibujarla en el mapa, esta codificada a codigo ascci
 */
data class Journey(
    val stops: List<Stop>,
    val firstDeparture: String,
    val lastDeparture: String,
    val encodedPolyline: String? = null
)
