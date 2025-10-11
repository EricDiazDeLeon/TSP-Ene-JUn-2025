package com.example.mirutadigital.data.remote

import com.example.mirutadigital.data.model.LiveTruck
import com.example.mirutadigital.data.model.Route
import com.example.mirutadigital.data.model.Stop
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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
        val snapshot = firestore.collection("routes").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val data = doc.data
                Route(
                    id = doc.id,
                    name = data?.get("name") as? String ?: "",
                    operatingHours = data?.get("operatingHours") as? String ?: "",
                    polylinePoints = emptyList() // Se cargará desde la base de datos local
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getStops(): List<Stop> {
        val snapshot = firestore.collection("stops").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val data = doc.data
                Stop(
                    id = doc.id,
                    name = data?.get("name") as? String ?: "",
                    location = com.google.android.gms.maps.model.LatLng(
                        (data?.get("latitude") as? Number)?.toDouble() ?: 0.0,
                        (data?.get("longitude") as? Number)?.toDouble() ?: 0.0
                    )
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
