package com.example.mirutadigital.data.remote

import android.util.Log
import com.example.mirutadigital.data.model.LiveTruck
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.model.Stop
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()

    fun getLiveTrucksFlow(): Flow<List<LiveTruck>> = callbackFlow {
        val listener = firestore.collection("liveTrucks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val trucks = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        LiveTruck(
                            truckId = doc.id,
                            routeId = data?.get("routeId") as? String ?: "",
                            location = com.google.android.gms.maps.model.LatLng(
                                (data?.get("latitude") as? Number)?.toDouble() ?: 0.0,
                                (data?.get("longitude") as? Number)?.toDouble() ?: 0.0
                            ),
                            lastUpdate = (data?.get("lastUpdate") as? Number)?.toLong() ?: 0L,
                            viewersCount = (data?.get("viewersCount") as? Number)?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(trucks)
            }

        awaitClose { listener.remove() }
    }

    suspend fun shareTruckLocation(truckId: String, routeId: String, latitude: Double, longitude: Double) {
        val data = mapOf(
            "routeId" to routeId,
            "latitude" to latitude,
            "longitude" to longitude,
            "lastUpdate" to System.currentTimeMillis(),
            "viewersCount" to 0
        )
        firestore.collection("liveTrucks").document(truckId).set(data).await()
    }

    suspend fun getRoutes(): List<Route> {
        return try {
            val snapshot = firestore.collection("routes").get().await()
            snapshot.documents.mapNotNull { doc ->
                // Aquí también leemos el GeoPoint correctamente
                val points = doc.get("polylinePoints") as? List<GeoPoint> ?: emptyList()
                Route(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    operatingHours = doc.getString("operatingHours") ?: "",
                    polylinePoints = points.map { LatLng(it.latitude, it.longitude) }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getStops(): List<Stop> {
        return try {
            val snapshot = firestore.collection("stops").get().await()
            snapshot.documents.mapNotNull { doc ->
                // --- LA CORRECCIÓN CLAVE ESTÁ AQUÍ ---
                val locationGeoPoint = doc.getGeoPoint("location") // Lee el campo GeoPoint
                val routeIds = doc.get("associatedRouteIds") as? List<String> ?: emptyList()

                if (locationGeoPoint != null) {
                    Stop(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        location = LatLng(locationGeoPoint.latitude, locationGeoPoint.longitude),
                        associatedRouteIds = routeIds
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreDebugger", "¡ERROR BRUTAL AL OBTENER PARADAS!", e)
            emptyList()
        }
    }
}
