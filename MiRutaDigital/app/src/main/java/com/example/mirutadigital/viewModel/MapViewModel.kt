package com.example.mirutadigital.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapViewModel : ViewModel() {
    // ubicacion inicial
    private val initialLocation = LatLng(22.7626, -102.5807)

    // aqui iria la logica para obtener los markadores del mapa y las polilineas
    // val markers = mutableStateListOf<MarkerInfo>()

    // lista observable de los marcadores
    val markers = mutableStateListOf<MarkerOptions>()

    init {
        // aqui se llama al repositorio para obtener los datos y añadir los marcadres a la lista
        //loadRoutes()
    }

    private fun loadRoutes() {
        // obtener los datos y añadirlos a la lista de marcadores

        // val routeMarkers = repository.getMarkersForRoute1()
        // markers.addAll(routeMarkers)
    }
}