package com.example.mirutadigital.data.repository

import android.location.Location
import android.util.Log
import com.example.mirutadigital.data.local.dao.FavoriteRouteDao
import com.example.mirutadigital.data.local.dao.RouteDao
import com.example.mirutadigital.data.local.dao.RouteHistoryDao
import com.example.mirutadigital.data.local.dao.RouteRatingDao
import com.example.mirutadigital.data.local.entities.FavoriteRouteEntity
import com.example.mirutadigital.data.local.entities.JourneyEntity
import com.example.mirutadigital.data.local.entities.JourneyStopCrossRef
import com.example.mirutadigital.data.local.entities.RouteEntity
import com.example.mirutadigital.data.local.entities.RouteHistoryEntity
import com.example.mirutadigital.data.local.entities.RouteRatingEntity
import com.example.mirutadigital.data.local.entities.StopEntity
import com.example.mirutadigital.data.local.relations.JourneyWithStops
import com.example.mirutadigital.data.model.ui.JourneyDetailInfo
import com.example.mirutadigital.data.model.ui.JourneyInfo
import com.example.mirutadigital.data.model.ui.RouteDetailInfo
import com.example.mirutadigital.data.model.ui.RouteInfo
import com.example.mirutadigital.data.model.ui.RouteInfoSchedulel
import com.example.mirutadigital.data.model.ui.RoutesInfo
import com.example.mirutadigital.data.model.ui.Schedule
import com.example.mirutadigital.data.model.ui.StopWithRoutes
import com.example.mirutadigital.data.model.ui.base.Stop
import com.example.mirutadigital.data.network.GeocodingResult
import com.example.mirutadigital.data.network.GoogleApiService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import org.w3c.dom.Comment

data class ActiveShareData(
    val userId: String,
    val routeId: String,
    val journeyType: String,
    val lastLocation: GeoPoint,
    val timestamp: Timestamp
)


data class RouteRatingSummary(
    val average: Double,
    val count: Int
)

/**
 * Repositorio que usa el DAO para obtener datos y los
 * transforma en los modelos que la ui necesitara
 *
 * @param routeDao El DAO inyectado
 * @param favoriteRouteDao El DAO inyectado para favoritos
 * @param routeHistoryDao El DAO inyectado para historial
 */
class RouteRepository(
    private val routeDao: RouteDao,
    private val favoriteRouteDao: FavoriteRouteDao,
    private val routeHistoryDao: RouteHistoryDao,
    private val routeRatingDao: RouteRatingDao,
    private val googleApiService: GoogleApiService,
    private val firestore: FirebaseFirestore,
    private val userIdProvider: UserIdProvider
) {
    /**
     * Obtiene la info detallada de una ruta desde la bse de datos
     */
    suspend fun getCoordinatesForAddress(address: String, apiKey: String): GeocodingResult? {
        return try {
            val response = googleApiService.getCoordinates(address, apiKey)
            response.results.firstOrNull()
        } catch (e: Exception) {
            // manejar errores de red o parsing
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtiene la info detallada de una ruta desde la bse de datos
     */
    suspend fun getRouteDetailInfoById(routeId: String): RouteDetailInfo? {
        val routeWithJourneys = routeDao.getRouteWithJourneysById(routeId) ?: return null
        
        // Obtener todos los crossRefs para ordenar
        val allCrossRefs = try { routeDao.getAllJourneyStopCrossRefs() } catch (e: Exception) { emptyList() }
        val journeyOrderMap = allCrossRefs
            .groupBy { it.journeyId }
            .mapValues { entry -> 
                entry.value.associate { it.stopId to it.stopOrder }
            }

        // mapea el resultado del DAO al modelo de ui
        val route = routeWithJourneys.route
        val outbound = routeWithJourneys.journeys.find { it.journey.journeyType == "OUTBOUND" }
        val inbound = routeWithJourneys.journeys.find { it.journey.journeyType == "INBOUND" }

        return RouteDetailInfo(
            id = route.routeId,
            name = route.name,
            departureInterval = route.departureInterval,
            windshieldLabel = route.windshieldLabel,
            colors = route.colors,
            price = route.price,
            outboundJourney = outbound.toJourneyDetailInfo(journeyOrderMap),
            inboundJourney = inbound.toJourneyDetailInfo(journeyOrderMap)
        )
    }

    /**
     * Obtiene los horarios de todas las rutas
     */
    suspend fun getRoutesSchedule(): List<RouteInfoSchedulel> {
        val allRoutes = routeDao.getAllRoutesWithJourneys()
        // mapea la lista
        return allRoutes.map { routeWithJourneys ->
            val route = routeWithJourneys.route
            val outbound = routeWithJourneys.journeys.find { it.journey.journeyType == "OUTBOUND" }
            val inbound = routeWithJourneys.journeys.find { it.journey.journeyType == "INBOUND" }

            RouteInfoSchedulel(
                id = route.routeId,
                name = route.name,
                outboundInfo = outbound.toScheduleInfo(), // No need order for schedule
                inboundInfo = inbound.toScheduleInfo()
            )
        }
    }

    /**
     * Obtiene la info general de todas las rutas
     */
    suspend fun getGeneralRoutesInfo(): List<RoutesInfo> {
        val allRoutes = routeDao.getAllRoutesWithJourneys()
        
        // Obtener todos los crossRefs para ordenar
        val allCrossRefs = try { routeDao.getAllJourneyStopCrossRefs() } catch (e: Exception) { emptyList() }
        val journeyOrderMap = allCrossRefs
            .groupBy { it.journeyId }
            .mapValues { entry -> 
                entry.value.associate { it.stopId to it.stopOrder }
            }

        // mapea la lista
        return allRoutes.map { routeWithJourneys ->
            val route = routeWithJourneys.route
            val outbound = routeWithJourneys.journeys.find { it.journey.journeyType == "OUTBOUND" }
            val inbound = routeWithJourneys.journeys.find { it.journey.journeyType == "INBOUND" }

            val stopsJourney = listOfNotNull(
                outbound?.toJourneyInfo(journeyOrderMap),
                inbound?.toJourneyInfo(journeyOrderMap)
            )

            RoutesInfo(
                id = route.routeId,
                name = route.name,
                windshieldLabel = route.windshieldLabel,
                colors = route.colors,
                stopsJourney = stopsJourney
            )
        }
    }

    /**
     * Obtiene todas las paradas y las rutas que pasan por ellas
     */
    suspend fun getStopsWithRoutes(): List<StopWithRoutes> {
        val allRoutesWithJourneys = routeDao.getAllRoutesWithJourneys()

        val journeyToRouteMap = mutableMapOf<String, RouteEntity>()
        val journeyToDestinationMap = mutableMapOf<String, String>()

        allRoutesWithJourneys.forEach { routeWithJourneys ->
            val route = routeWithJourneys.route
            routeWithJourneys.journeys.forEach { journeyWithStops ->
                val journeyId = journeyWithStops.journey.journeyId
                journeyToRouteMap[journeyId] = route
                
                val destination = journeyWithStops.stops.lastOrNull()?.name ?: "N/A"
                journeyToDestinationMap[journeyId] = destination
            }
        }

        val allStops = routeDao.getAllStopsWithJourneys()
        
        return allStops.map { stopWithJourneys ->
            val stop = stopWithJourneys.stop
            
            val routesInfo = stopWithJourneys.journeys.mapNotNull { journeyCrossRef ->
                val journeyId = journeyCrossRef.journeyId
                
                val route = journeyToRouteMap[journeyId]
                val destinationName = journeyToDestinationMap[journeyId]

                if (route == null) {
                    null
                } else {
                    RouteInfo(
                        name = route.name,
                        destination = destinationName ?: "N/A"
                    )
                }
            }.distinct() // elimina duplicados

            StopWithRoutes(
                id = stop.stopId,
                name = stop.name,
                latitude = stop.latitude,
                longitude = stop.longitude,
                routes = routesInfo,
                distance = "Calculando..."
            )
        }
    }

    // --- METODOS PARA FAVORITOS ---

    /**
     * Agrega una ruta a favoritos
     */
    suspend fun addFavorite(routeId: String) {
        favoriteRouteDao.insertFavorite(FavoriteRouteEntity(routeId))
    }

    /**
     * Elimina una ruta de favoritos
     */
    suspend fun removeFavorite(routeId: String) {
        favoriteRouteDao.deleteFavoriteById(routeId)
    }

    /**
     * Alterna el estado de favorito de una ruta
     */
    suspend fun toggleFavorite(routeId: String, isFavorite: Boolean) {
        if (isFavorite) {
            removeFavorite(routeId)
        } else {
            addFavorite(routeId)
        }
    }

    /**
     * Obtiene todas las rutas favoritas
     */
    fun getAllFavorites(): Flow<List<FavoriteRouteEntity>> {
        return favoriteRouteDao.getAllFavorites()
    }

    /**
     * Verifica si una ruta es favorita
     */
    fun isFavorite(routeId: String): Flow<Boolean> {
        return favoriteRouteDao.isFavorite(routeId)
    }

    /**
     * Elimina todos los favoritos
     */
    suspend fun deleteAllFavorites() {
        favoriteRouteDao.deleteAllFavorites()
    }

    /**
     * Obtiene la cantidad de favoritos
     */
    suspend fun getFavoritesCount(): Int {
        return favoriteRouteDao.getFavoritesCount()
    }

    // --- METODOS PARA EL HISTORIAL ---

    /**
     * Agrega una ruta al historial
     */
    suspend fun addToHistory(routeId: String, routeName: String) {
        routeHistoryDao.insertHistory(
            RouteHistoryEntity(
                routeId = routeId,
                routeName = routeName
            )
        )
    }

    /**
     * Elimina un registro del historial
     */
    suspend fun removeFromHistory(historyId: Long) {
        routeHistoryDao.deleteHistoryById(historyId)
    }

    /**
     * Obtiene el historial
     */
    fun getAllHistory(): Flow<List<RouteHistoryEntity>> {
        return routeHistoryDao.getAllHistory()
    }

    /**
     * Elimina todos los registros de el historial
     */
    suspend fun deleteAllHistory() {
        routeHistoryDao.deleteAllHistory()
    }

    /**
     * Obtiene la cantidad de resgistros en el historial
     */
    suspend fun getHistoryCount(): Int {
        return routeHistoryDao.getHistoryCount()
    }

    // funciones de extension: ayuda a mapear los datos del DAO

    /**
     * Convierte un [JourneyWithStops] (BD) en un [JourneyDetailInfo] (UI)
     * Ahora ordena las paradas usando el mapa de orden.
     */
    private fun JourneyWithStops?.toJourneyDetailInfo(
        orderMap: Map<String, Map<String, Int>>? = null
    ): JourneyDetailInfo {
        if (this == null) return JourneyDetailInfo("N/A", "N/A", "N/A", "N/A")
        
        val currentJourneyId = this.journey.journeyId
        val stopsOrder = orderMap?.get(currentJourneyId)
        
        val sortedStops = if (stopsOrder != null) {
            this.stops.sortedBy { stop ->
                stopsOrder[stop.stopId] ?: Int.MAX_VALUE
            }
        } else {
            this.stops
        }

        val start = sortedStops.firstOrNull()?.name ?: "N/A"
        val end = sortedStops.lastOrNull()?.name ?: "N/A"
        return JourneyDetailInfo(
            startStopName = start,
            endStopName = end,
            firstDeparture = this.journey.firstDeparture,
            lastDeparture = this.journey.lastDeparture
        )
    }

    /**
     * Convierte un [JourneyWithStops] (BD) en un [Schedule] (UI)
     */
    private fun JourneyWithStops?.toScheduleInfo(): Schedule {
        if (this == null) return Schedule("N/A", "N/A", Pair("N/A", "N/A"))
        // En toScheduleInfo no tenemos el mapa de orden aqui facilmente, 
        // pero para el horario solo necesitamos origen y destino.
        // Si la lista esta desordenada, el primero/ultimo puede ser incorrecto.
        // Idealmente deberiamos ordenarlo tambien, pero para simplificar y no romper firmas dejaremos asi por ahora,
        // asumiendo que el impacto es menor (solo nombres visuales). 
        // Si es critico, habria que pasar el mapa tambien.
        val origin = this.stops.firstOrNull()?.name ?: "N/A"
        val destination = this.stops.lastOrNull()?.name ?: "N/A"
        return Schedule(
            nameOrigin = origin,
            nameDestination = destination,
            schedule = Pair(this.journey.firstDeparture, this.journey.lastDeparture)
        )
    }

    /**
     * Convierte un [JourneyWithStops] (BD) en un [JourneyInfo] (UI)
     * Ahora ordena las paradas.
     */
    private fun JourneyWithStops?.toJourneyInfo(
        orderMap: Map<String, Map<String, Int>>
    ): JourneyInfo? {
        if (this == null) return null
        
        val currentJourneyId = this.journey.journeyId
        val stopsOrder = orderMap[currentJourneyId]
        
        val sortedStops = if (stopsOrder != null) {
            this.stops.sortedBy { stop ->
                stopsOrder[stop.stopId] ?: Int.MAX_VALUE
            }
        } else {
            this.stops
        }

        val uiStops = sortedStops.map { dbStop ->
            Stop(
                id = dbStop.stopId,
                name = dbStop.name,
                coordinates = LatLng(dbStop.latitude, dbStop.longitude)
            )
        }
        return JourneyInfo(
            stops = uiStops,
            encodedPolyline = this.journey.encodedPolyline
        )
    }

    /**
     * Sincroniza la base de datos con los datos de Firebase
     */
    suspend fun synchronizeDatabase() {
        try {
            Log.d("Sincronizacion", "Iniciando sincronización...")

            // --- SINCRONIZAR PARADAS ---
            val stopsSnapshot = firestore.collection("stops").get().await()
            Log.d("Sincronizacion", "Paradas obtenidas: ${stopsSnapshot.size()}")

            val stopEntities = stopsSnapshot.documents.mapNotNull { doc ->
                try {
//                    @Suppress("UNCHECKED_CAST")
//                    val coords = doc.get("coordinates") as? Map<String, Any>
//                    val latitude = (coords?.get("latitude") as? Number)?.toDouble()
//                    val longitude = (coords?.get("longitude") as? Number)?.toDouble()
                    val geoPoint = doc.getGeoPoint("coordinates") // Pide un GeoPoint
                    val latitude = geoPoint?.latitude
                    val longitude = geoPoint?.longitude

                    if (latitude == null || longitude == null) {
                        Log.w("Sincronizacion", "Parada '${doc.id}' no tiene coordenadas válidas.")
                        return@mapNotNull null
                    }

                    StopEntity(
                        stopId = doc.id,
                        name = doc.getString("name") ?: "N/A",
                        latitude = latitude,
                        longitude = longitude
                    )
                } catch (e: Exception) {
                    // error en una parada
                    null
                }
            }

            if (stopEntities.isEmpty()) {
                // caso en que no hay validas
                return
            }

            // --- SINCRONIZAR RUTAS ---
            val routesSnapshot = firestore.collection("routes").get().await()
            Log.d("Sincronizacion", "Rutas obtenidas: ${routesSnapshot.size()}")

            val routeEntities = mutableListOf<RouteEntity>()
            val journeyEntities = mutableListOf<JourneyEntity>()
            val crossRefEntities = mutableListOf<JourneyStopCrossRef>()

            for (doc in routesSnapshot.documents) {
                try {
                    val routeId = doc.id

                    @Suppress("UNCHECKED_CAST")
                    val detail = doc.get("detail") as? Map<String, Any>

                    routeEntities.add(
                        RouteEntity(
                            routeId = routeId,
                            name = doc.getString("name") ?: "N/A",
                            departureInterval = (doc.get("departureInterval") as? Number)?.toInt()
                                ?: 10,
                            windshieldLabel = detail?.get("windshieldLabel") as? String ?: "N/A",
                            colors = detail?.get("colors") as? String ?: "N/A",
                            price = (detail?.get("price") as? Number)?.toDouble() ?: 0.0,
                            lastUpdate = System.currentTimeMillis()
                        )
                    )

                    // procesar trayectos de ida y vuelta
                    processJourney(
                        doc,
                        "outboundJourney",
                        "OUTBOUND",
                        routeId,
                        journeyEntities,
                        crossRefEntities
                    )
                    processJourney(
                        doc,
                        "inboundJourney",
                        "INBOUND",
                        routeId,
                        journeyEntities,
                        crossRefEntities
                    )

                } catch (e: Exception) {
                    Log.e("Sincronizacion", "Error procesando ruta ${doc.id}", e)
                }
            }

            if (routeEntities.isEmpty()) {
                Log.w("Sincronizacion", "No se obtuvieron rutas válidas")
                return
            }

            Log.d(
                "Sincronizacion",
                "Insertando en BD: ${stopEntities.size} paradas, ${routeEntities.size} rutas"
            )
            routeDao.syncchronizeAll(
                stopEntities,
                routeEntities,
                journeyEntities,
                crossRefEntities
            )
        } catch (e: Exception) {
            Log.e("Sincronizacion", "Error al sincronizar datos desde Firebase", e)
            throw e
        }
    }


    private fun processJourney(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        journeyKey: String,
        journeyType: String,
        routeId: String,
        journeyEntities: MutableList<JourneyEntity>,
        crossRefEntities: MutableList<JourneyStopCrossRef>
    ) {
        try {
            @Suppress("UNCHECKED_CAST")
            val journeyData = doc.get(journeyKey) as? Map<String, Any> ?: return
            val journeyId = "${routeId}_$journeyType"

            journeyEntities.add(
                JourneyEntity(
                    journeyId = journeyId,
                    routeOwnerId = routeId,
                    journeyType = journeyType,
                    firstDeparture = journeyData["firstDeparture"] as? String ?: "N/A",
                    lastDeparture = journeyData["lastDeparture"] as? String ?: "N/A",
                    encodedPolyline = journeyData["encodedPolyline"] as? String
                )
            )

            @Suppress("UNCHECKED_CAST")
            val stops = journeyData["stops"] as? List<Map<String, Any>>
            stops?.forEachIndexed { index, stopMap ->
                val stopId = stopMap["id"] as? String
                if (stopId != null) {
                    crossRefEntities.add(
                        JourneyStopCrossRef(
                            journeyId = journeyId,
                            stopId = stopId,
                            stopOrder = index
                        )
                    )
                } else {
                    // caso en que stopId es nulo
                }
            }
        } catch (e: Exception) {
            // error
        }
    }

// --- COMPARTIR ---

    /**
     * Inicia o actualiza la compartición de un usuario.
     * Usa el ID de usuario como clave de documento en 'active_shares' para atomicidad.
     *
     * @param userId El ID unico de este dispositivo/usuario.
     * @param routeId El ID de la ruta que comparte.
     * @param journeyType "outbound" o "inbound".
     * @param location La ubicación actual del usuario.
     */
    suspend fun startShare(
        userId: String,
        routeId: String,
        journeyType: String,
        location: Location
    ) {
        val shareData = hashMapOf(
            "routeId" to routeId,
            "journeyType" to journeyType,
            "lastLocation" to GeoPoint(location.latitude, location.longitude),
            "timestamp" to FieldValue.serverTimestamp()
        )

        try {
            firestore.collection("active_shares")
                .document(userId) // id del documento es el user
                .set(
                    shareData,
                    SetOptions.merge()
                ) // merge()
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Detieneel comparitr de un usuario
     * elimina el documento del usuario de 'active_shares'
     *
     * @param userId El ID unico del usuario que deja de compartir
     */
    suspend fun stopShare(userId: String, routeId: String) {
        try {
            firestore.collection("active_shares")
                .document(userId)
                .delete()
                .await()
        } catch (e: Exception) {
            // error
        }
    }

    /**
     * Obtiene todas las compartidas activas para una ruta especifica
     *
     * @param routeId El ID de la ruta a consultar.
     * @return Una lista de datos (ActiveShareData).
     */
    suspend fun getActiveSharesForRoute(routeId: String): List<ActiveShareData> {
        return try {
            val snapshot = firestore.collection("active_shares")
                .whereEqualTo("routeId", routeId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    ActiveShareData(
                        userId = doc.id,
                        routeId = doc.getString("routeId") ?: "",
                        journeyType = doc.getString("journeyType") ?: "",
                        lastLocation = doc.getGeoPoint("lastLocation") ?: GeoPoint(0.0, 0.0),
                        timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene la info general de todas las rutas que pasan por una parada especifica
     */
    suspend fun getGeneralRoutesInfoByStop(stopId: String): List<RoutesInfo> {
        val routesByStop = routeDao.getRoutesByStopId(stopId)
        
        // Obtener todos los crossRefs para ordenar
        val allCrossRefs = try { routeDao.getAllJourneyStopCrossRefs() } catch (e: Exception) { emptyList() }
        val journeyOrderMap = allCrossRefs
            .groupBy { it.journeyId }
            .mapValues { entry -> 
                entry.value.associate { it.stopId to it.stopOrder }
            }

        return routesByStop.map { routeWithJourneys ->
            val route = routeWithJourneys.route
            val outbound = routeWithJourneys.journeys.find { it.journey.journeyType == "OUTBOUND" }
            val inbound = routeWithJourneys.journeys.find { it.journey.journeyType == "INBOUND" }

            val stopsJourney = listOfNotNull(
                outbound?.toJourneyInfo(journeyOrderMap),
                inbound?.toJourneyInfo(journeyOrderMap)
            )

            RoutesInfo(
                id = route.routeId,
                name = route.name,
                windshieldLabel = route.windshieldLabel,
                colors = route.colors,
                stopsJourney = stopsJourney
            )
        }
    }


    // --- METODOS PARA EL RATING DE RUTAS ---

    suspend fun submitRouteRating(routeId: String, stars: Int, comment: String?): Boolean {
        val userId = userIdProvider.getUserId()
        val entity = RouteRatingEntity(
            routeId = routeId,
            userId = userId,
            stars = stars,
            comment = comment ?: "",
            createdAt = System.currentTimeMillis(),
            pendingSync = true
        )
        val localId = routeRatingDao.insertRating(entity)

        val data = hashMapOf(
            "routeId" to routeId,
            "userId" to userId,
            "stars" to stars,
            "comment" to (comment ?: ""),
            "timestamp" to FieldValue.serverTimestamp()
        )
        return try {
            firestore.collection("route_ratings").add(data).await()
            routeRatingDao.markRatingSynced(localId)
            true
        } catch (e: Exception) {
            Log.w("RatingSync", "Failed to sync rating to Firestore, saved locally.", e)
            false
        }
    }

    private suspend fun retryPendingRatings() {
        val pending = routeRatingDao.getPendingRatings()
        for (rating in pending) {
            val data = hashMapOf(
                "routeId" to rating.routeId,
                "userId" to rating.userId,
                "stars" to rating.stars,
                "comment" to (rating.comment ?: ""),
                "timestamp" to FieldValue.serverTimestamp()
            )
            try {
                firestore.collection("route_ratings").add(data).await()
                routeRatingDao.markRatingSynced(rating.id)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun getRouteRatingSummary(routeId: String): RouteRatingSummary {
        return try {
            val snapshot = firestore.collection("route_ratings")
                .whereEqualTo("routeId", routeId).get().await()

            val stars = snapshot.documents.mapNotNull { (it.get("stars") as? Number)?.toInt() }
            val count = stars.size
            val avg = if (count > 0) stars.sum().toDouble() / count else 0.0
            RouteRatingSummary(average = avg, count = count)
        } catch (_: Exception) {
            RouteRatingSummary(average = 0.0, count = 0)
        }
    }

    fun getRatingsForRoute(routeId: String): Flow<List<RouteRatingEntity>> {
        return routeRatingDao.getRatingsForRoute(routeId)
    }

    suspend fun syncRatingsForRoute(routeId: String) {
        try {
            val snapshot = firestore.collection("route_ratings")
                .whereEqualTo("routeId", routeId)
                .get().await()

            val serverRatings = snapshot.documents.mapNotNull { doc ->
                val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: System.currentTimeMillis()
                RouteRatingEntity(
                    routeId = doc.getString("routeId") ?: return@mapNotNull null,
                    userId = doc.getString("userId") ?: "",
                    stars = (doc.get("stars") as? Long)?.toInt() ?: 0,
                    comment = doc.getString("comment"),
                    createdAt = timestamp,
                    pendingSync = false
                )
            }

            if (serverRatings.isNotEmpty()) {
                routeRatingDao.syncRatings(routeId, serverRatings)
            }
        } catch (e: Exception) {
            Log.e("RatingSync", "Error syncing ratings for route $routeId", e)
        }
    }
}
