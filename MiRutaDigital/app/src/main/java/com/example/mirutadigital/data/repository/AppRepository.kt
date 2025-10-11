package com.example.mirutadigital.data.repository

import com.example.mirutadigital.data.local.AppDao
import com.example.mirutadigital.data.local.RouteEntity
import com.example.mirutadigital.data.local.StopEntity
import com.example.mirutadigital.data.model.LiveTruck
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.model.Stop
import com.example.mirutadigital.data.remote.FirestoreService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(
    private val appDao: AppDao,
    private val firestoreService: FirestoreService
) {
    fun getRoutes(): Flow<List<Route>> {
        return appDao.getAllRoutes().map { entities ->
            entities.map { entity ->
                Route(
                    id = entity.id,
                    name = entity.name,
                    operatingHours = entity.operatingHours,
                    polylinePoints = emptyList() // Se cargará desde la base de datos local
                )
            }
        }
    }

    fun getStops(): Flow<List<Stop>> {
        return appDao.getAllStops().map { entities ->
            entities.map { entity ->
                Stop(
                    id = entity.id,
                    name = entity.name,
                    location = LatLng(entity.latitude, entity.longitude)
                )
            }
        }
    }

    fun getLiveTrucks(): Flow<List<LiveTruck>> {
        return firestoreService.getLiveTrucksFlow()
    }

    suspend fun shareLocation(truckId: String, routeId: String, latitude: Double, longitude: Double) {
        firestoreService.shareTruckLocation(truckId, routeId, latitude, longitude)
    }

    fun getAvailableRoutes(): Flow<List<Route>> {
        return getRoutes().map { routes ->
            val currentTime = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTimeString = timeFormat.format(currentTime.time)

            routes.filter { route ->
                isRouteAvailable(route.operatingHours, currentTimeString)
            }
        }
    }

    fun getLiveTruckDetails(truckId: String): Flow<LiveTruck?> {
        return firestoreService.getLiveTrucksFlow().map { trucks ->
            trucks.find { it.truckId == truckId }
        }
    }

    private fun isRouteAvailable(operatingHours: String, currentTime: String): Boolean {
        // Implementar lógica para verificar si la ruta está disponible en el horario actual
        // Por ahora, retornamos true para todas las rutas
        return true
    }

    suspend fun refreshRoutes() {
        val routes = firestoreService.getRoutes()
        val routeEntities = routes.map { route ->
            RouteEntity(
                id = route.id,
                name = route.name,
                operatingHours = route.operatingHours,
                polylinePointsJson = "" // Se llenará con datos reales
            )
        }
        appDao.insertAllRoutes(routeEntities)
    }

    suspend fun refreshStops() {
        val stops = firestoreService.getStops()
        val stopEntities = stops.map { stop ->
            StopEntity(
                id = stop.id,
                name = stop.name,
                latitude = stop.location.latitude,
                longitude = stop.location.longitude
            )
        }
        appDao.insertAllStops(stopEntities)
    }
}
