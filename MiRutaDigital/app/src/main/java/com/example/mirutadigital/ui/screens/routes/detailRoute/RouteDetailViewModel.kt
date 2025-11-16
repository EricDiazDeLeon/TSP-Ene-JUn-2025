package com.example.mirutadigital.ui.screens.routes.detailRoute

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.MiRutaApplication
import com.example.mirutadigital.data.model.ui.RouteDetailInfo
import com.example.mirutadigital.data.model.ui.RoutesInfo
import com.example.mirutadigital.data.model.ui.base.Stop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class RouteDetailUiState(
    val route: RouteDetailInfo? = null,
    val routeMap: RoutesInfo? = null,
    val isLoading: Boolean = true,
    val etaResult: String? = null,
    val selectedStopId: String? = null,
    val isFavorite: Boolean = false,
    val showRatingDialog: Boolean = false,
    val ratingStars: Int = 0,
    val ratingComment: String = "",
    val ratingMessage: String? = null,
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0
)

class RouteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiRutaApplication).repository

    private val _uiState = MutableStateFlow(RouteDetailUiState())
    val uiState: StateFlow<RouteDetailUiState> = _uiState.asStateFlow()

    fun loadRouteById(routeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, etaResult = null) }

            val route = repository.getRouteDetailInfoById(routeId) // getRouteDetailInfoById()
            val routeMap =
                repository.getGeneralRoutesInfo().find { it.id == routeId }

            repository.isFavorite(routeId).collect { isFavorite ->
                val summary = repository.getRouteRatingSummary(routeId)
                _uiState.update {
                    it.copy(
                        route = route,
                        routeMap = routeMap,
                        // fullRoute = fullRoute,
                        isLoading = false,
                        etaResult = null,
                        isFavorite = isFavorite,
                        ratingAverage = summary.average,
                        ratingCount = summary.count
                    )
                }
            }
        }
    }

    fun toggleFavorite(routeId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(routeId, _uiState.value.isFavorite)
        }
    }

    fun openRatingDialog() {
        _uiState.update { it.copy(showRatingDialog = true, ratingMessage = null) }
    }

    fun closeRatingDialog() {
        _uiState.update { it.copy(showRatingDialog = false) }
    }

    fun setRatingStars(stars: Int) {
        _uiState.update { it.copy(ratingStars = stars.coerceIn(0, 5)) }
    }

    fun setRatingComment(comment: String) {
        _uiState.update { it.copy(ratingComment = comment) }
    }

    fun submitRating(routeId: String) {
        val stars = _uiState.value.ratingStars
        val comment = _uiState.value.ratingComment
        if (stars < 1) {
            _uiState.update { it.copy(ratingMessage = "Por favor selecciona al menos una estrella.") }
            return
        }
        viewModelScope.launch {
            val ok = repository.submitRouteRating(routeId, stars, comment)
            if (ok) {
                _uiState.update { it.copy(showRatingDialog = false, ratingStars = 0, ratingComment = "", ratingMessage = "¡Gracias por tu retroalimentación!") }
            } else {
                _uiState.update { it.copy(showRatingDialog = false, ratingMessage = "No se pudo enviar la calificación. Se guardará y se intentará más tarde.") }
            }
        }
    }

    /**
     * Actualiza el UIState para mostrar qué parada está seleccionada (estado pendiente)
     * Esto se llama cuando el usuario da clic en una parada en el mapa
     */
    fun updateSelectedStopInfo(stopId: String?) {
        val routeMap = _uiState.value.routeMap
        if (stopId == null || routeMap == null) {
            _uiState.update { it.copy(etaResult = "ETA: sin calcular...") }
            return
        }

        _uiState.update {
            it.copy(
                etaResult = "ETA: sin calcular...",
                selectedStopId = stopId
            )
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun calculateDistance(a: Stop, b: Stop): Float {
        val loc1 = Location("").apply {
            latitude = a.coordinates.latitude
            longitude = a.coordinates.longitude
        }
        val loc2 = Location("").apply {
            latitude = b.coordinates.latitude
            longitude = b.coordinates.longitude
        }
        return loc1.distanceTo(loc2)
    }

    /**
     * Calcula el ETA de la parada seleccionada considerando:
        - La distancia total hasta la parada y entre paradas
        - Una velocidad promedio (constante),
        - El horario de salida (primera y ultima)
        - La hora actual
         definicion: la predicción del tiempo estimado de llegada,
         con base en la información disponible
     */
    fun calculateEta() { //stopId: String?
        val stopId = _uiState.value.selectedStopId

        val route = _uiState.value.route ?: return
        val routeMap = _uiState.value.routeMap ?: return

        if (stopId == null) {
            _uiState.update { it.copy(etaResult = "ETA: Elige una parada") }
            return
        }

        val isOutbound = routeMap.stopsJourney.getOrNull(0)?.stops?.any { it.id == stopId } == true

        val stops = if (isOutbound)
            routeMap.stopsJourney.getOrNull(0)?.stops.orEmpty()
        else
            routeMap.stopsJourney.getOrNull(1)?.stops.orEmpty()

        val intervalMinutes = route.departureInterval.toLong()
        val journey = if (isOutbound) route.outboundJourney else route.inboundJourney

        val index = stops.indexOfFirst { it.id == stopId }
        if (index < 0) {
            _uiState.update { it.copy(etaResult = "ETA: Elige una parada") }
            return
        }

        var totalMeters = 0f
        for (i in 0 until index.coerceAtMost(stops.size - 1)) {
            totalMeters += calculateDistance(a = stops[i], b = stops[i + 1])
        }

        val busSpeedMps = 20_000 / 3600.0 // 20 km/h en m/s
        val travelTimeSeconds = totalMeters / busSpeedMps

        val formatter = DateTimeFormatter.ofPattern("H:mm")
        val firstDeparture = LocalTime.parse(journey.firstDeparture, formatter)
        val lastDeparture = LocalTime.parse(journey.lastDeparture, formatter)

        val now = LocalTime.now()
        val today = LocalDate.now()

        var departure = firstDeparture
        var chosenDeparture: LocalTime? = null

        while (!departure.isAfter(lastDeparture)) {
            val arrival = departure.plusSeconds(travelTimeSeconds.toLong())
            if (arrival.isAfter(now)) {
                chosenDeparture = arrival
                break
            }
            departure = departure.plusMinutes(intervalMinutes)
        }

        if (chosenDeparture == null) chosenDeparture = firstDeparture


        val etaDateTime = LocalDateTime.of(today, chosenDeparture)
            .plusSeconds(travelTimeSeconds.toLong())

        val diffDuration = Duration.between(LocalDateTime.now(), etaDateTime)
        var diffMinutes = diffDuration.toMinutes().toInt()
        val diffSeconds = (diffDuration.seconds % 60).toInt()
//        var diffMinutes = Duration.between(LocalDateTime.now(), etaDateTime).toMinutes().toInt()
        if (diffMinutes < 0) diffMinutes += 24 * 60 // un dia


        val etaText = when {
            diffMinutes < 60 -> "Llega en $diffMinutes min ${diffSeconds}s"
            else -> "Llega en ${diffMinutes / 60} h ${diffMinutes % 60} min"
        }

        _uiState.update {
            it.copy(etaResult = "ETA: $etaText")
        }
    }
}











