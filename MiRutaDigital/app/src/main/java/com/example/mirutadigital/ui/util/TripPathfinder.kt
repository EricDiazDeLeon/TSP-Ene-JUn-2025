package com.example.mirutadigital.ui.util

import android.location.Location
import androidx.compose.ui.graphics.Color
import com.example.mirutadigital.data.model.ui.RoutesInfo
import com.example.mirutadigital.data.model.ui.base.Stop
import com.example.mirutadigital.ui.screens.tripPlan.BusStep
import com.example.mirutadigital.ui.screens.tripPlan.TripItinerary
import com.example.mirutadigital.ui.screens.tripPlan.TripStep
import com.example.mirutadigital.ui.screens.tripPlan.WalkStep
import com.google.android.gms.maps.model.LatLng
import java.util.PriorityQueue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object PathfinderConfig {
    const val MAX_WALKING_DISTANCE_METERS = 1500.0 //1500m
    const val WALKING_SPEED_MPS = 1.2 // ~4.3 km/h
    const val BUS_SPEED_MPS = 5.5 // ~20 km/h

    const val TRANSFER_PENALTY_SECONDS = 1500.0 // 25 min
    
    const val BOARDING_PENALTY_SECONDS = 120.0 // 2 minutos
    const val WRONG_DIRECTION_PENALTY = 3600.0 // 1 hora
}

data class GraphNode(
    val stopId: String,
    val stopName: String,
    val lat: Double,
    val lng: Double,
    val edges: MutableList<Edge> = mutableListOf()
)

data class Edge(
    val targetNodeId: String,
    val weightSeconds: Double,
    val type: EdgeType,
    val routeId: String? = null,
    val routeName: String? = null,
    val direction: String? = null, // "Ida" o "Vuelta"
    val distanceMeters: Double
)

enum class EdgeType { WALKING, BUS }

class TripPathfinder {

    private val graph = HashMap<String, GraphNode>()

    fun buildGraph(stops: List<Stop>, routes: List<RoutesInfo>) {
        graph.clear()

        stops.forEach { stop ->
            graph[stop.id] = GraphNode(
                stopId = stop.id,
                stopName = stop.name,
                lat = stop.coordinates.latitude,
                lng = stop.coordinates.longitude
            )
        }

        routes.forEach { route ->
            addBusEdgesForJourney(route.stopsJourney.getOrNull(0)?.stops, route, "Ida")
            addBusEdgesForJourney(route.stopsJourney.getOrNull(1)?.stops, route, "Vuelta")
        }

        val stopsList = graph.values.toList()
        val latLngDiff = 0.015
        
        stopsList.forEach { origin ->
            stopsList.forEach { target ->
                if (origin.stopId != target.stopId) {
                    if (Math.abs(origin.lat - target.lat) < latLngDiff && Math.abs(origin.lng - target.lng) < latLngDiff) {
                        val distance = calculateDistance(origin.lat, origin.lng, target.lat, target.lng)
                        if (distance <= PathfinderConfig.MAX_WALKING_DISTANCE_METERS) {
                            val timeSeconds = distance / PathfinderConfig.WALKING_SPEED_MPS
                            origin.edges.add(
                                Edge(
                                    targetNodeId = target.stopId,
                                    weightSeconds = timeSeconds,
                                    type = EdgeType.WALKING,
                                    distanceMeters = distance
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun addBusEdgesForJourney(stops: List<Stop>?, route: RoutesInfo, direction: String) {
        if (stops == null || stops.size < 2) return

        for (i in 0 until stops.size - 1) {
            val originStop = graph[stops[i].id]
            val targetStop = graph[stops[i + 1].id]

            if (originStop != null && targetStop != null) {
                val distance = calculateDistance(originStop.lat, originStop.lng, targetStop.lat, targetStop.lng)
                val timeSeconds = distance / PathfinderConfig.BUS_SPEED_MPS
                
                originStop.edges.add(
                    Edge(
                        targetNodeId = targetStop.stopId,
                        weightSeconds = timeSeconds,
                        type = EdgeType.BUS,
                        routeId = route.id,
                        routeName = route.name,
                        direction = direction,
                        distanceMeters = distance
                    )
                )
            }
        }
    }

    fun findPath(origin: LatLng, destination: LatLng): TripItinerary? {
        val directDistance = calculateDistance(origin.latitude, origin.longitude, destination.latitude, destination.longitude)
        
        val startNodeIds = findNearbyStops(origin)
        val endNodeIds = findNearbyStops(destination)

        if ((startNodeIds.isEmpty() || endNodeIds.isEmpty()) && directDistance < 2000) {
             val walkTimeMinutes = (directDistance / PathfinderConfig.WALKING_SPEED_MPS / 60).toInt()
             return TripItinerary(
                totalDurationMinutes = walkTimeMinutes,
                startTime = "Ahora",
                endTime = "+$walkTimeMinutes min",
                steps = listOf(
                    WalkStep(
                        durationMinutes = walkTimeMinutes,
                        distanceMeters = directDistance.toInt(),
                        description = "Caminar directo",
                        instructions = "Camina directamente hacia tu destino"
                    )
                )
            )
        }

        if (startNodeIds.isEmpty() || endNodeIds.isEmpty()) return null

        val minTime = HashMap<String, Double>()
        val previous = HashMap<String, PreviousStep>()
        val pq = PriorityQueue<QueueNode>()

        graph.keys.forEach { minTime[it] = Double.MAX_VALUE }

        startNodeIds.forEach { (stopId, distToStop) ->
            val walkTime = distToStop / PathfinderConfig.WALKING_SPEED_MPS
            minTime[stopId] = walkTime
            pq.add(QueueNode(stopId, walkTime))
            previous[stopId] = PreviousStep(null, Edge(stopId, walkTime, EdgeType.WALKING, distanceMeters = distToStop), isStartWalk = true)
        }

        var destinationNodeId: String? = null
        var finalTotalTime = Double.MAX_VALUE
        
        if (directDistance < 3000) { 
             finalTotalTime = directDistance / PathfinderConfig.WALKING_SPEED_MPS
        }

        while (pq.isNotEmpty()) {
            val (currentNodeId, currentTime) = pq.poll()

            if (currentTime > minTime[currentNodeId]!!) continue

            val distToDest = endNodeIds.find { it.first == currentNodeId }?.second
            if (distToDest != null) {
                val finalWalkTime = distToDest / PathfinderConfig.WALKING_SPEED_MPS
                val totalTripTime = currentTime + finalWalkTime
                
                if (totalTripTime < finalTotalTime) {
                    finalTotalTime = totalTripTime
                    destinationNodeId = currentNodeId
                    previous["DESTINATION"] = PreviousStep(currentNodeId, Edge("DESTINATION", finalWalkTime, EdgeType.WALKING, distanceMeters = distToDest))
                }
                continue
            }

            val currentNode = graph[currentNodeId] ?: continue

            for (edge in currentNode.edges) {
                var weight = edge.weightSeconds
                val prevStep = previous[currentNodeId]

                if (edge.type == EdgeType.BUS) {
                     if (prevStep?.edge?.type == EdgeType.WALKING || prevStep?.isStartWalk == true) {
                         weight += PathfinderConfig.BOARDING_PENALTY_SECONDS
                     } 
                     else if (prevStep?.edge?.type == EdgeType.BUS) {
                         val prevRoute = prevStep.edge.routeId
                         val currentRoute = edge.routeId
                         
                         if (prevRoute == currentRoute) {
                             if (prevStep.edge.direction != edge.direction) {
                                 weight += PathfinderConfig.WRONG_DIRECTION_PENALTY
                             }
                         } else {
                             weight += PathfinderConfig.TRANSFER_PENALTY_SECONDS
                         }
                     }
                }

                val newTime = currentTime + weight

                if (newTime < minTime[edge.targetNodeId]!!) {
                    minTime[edge.targetNodeId] = newTime
                    previous[edge.targetNodeId] = PreviousStep(currentNodeId, edge)
                    pq.add(QueueNode(edge.targetNodeId, newTime))
                }
            }
        }

        if (destinationNodeId == null && directDistance < 3000) {
             val walkTimeMinutes = (directDistance / PathfinderConfig.WALKING_SPEED_MPS / 60).toInt()
             return TripItinerary(
                totalDurationMinutes = walkTimeMinutes,
                startTime = "Ahora",
                endTime = "+$walkTimeMinutes min",
                steps = listOf(
                    WalkStep(
                        durationMinutes = walkTimeMinutes,
                        distanceMeters = directDistance.toInt(),
                        description = "Caminar",
                        instructions = "Es más rápido caminar directamente"
                    )
                )
            )
        }

        if (destinationNodeId == null) return null

        return reconstructPath(previous, destinationNodeId, finalTotalTime)
    }

    private fun findNearbyStops(location: LatLng): List<Pair<String, Double>> {
        return graph.values.map { node ->
            val dist = calculateDistance(location.latitude, location.longitude, node.lat, node.lng)
            node.stopId to dist
        }.filter { it.second <= PathfinderConfig.MAX_WALKING_DISTANCE_METERS }
         .sortedBy { it.second }
    }

    private fun reconstructPath(previous: HashMap<String, PreviousStep>, lastStopId: String, totalTimeSeconds: Double): TripItinerary {
        val rawSteps = mutableListOf<TripStep>()
        var current = "DESTINATION"
        
        while (current != "START") {
            val prevStep = previous[current] ?: break
            
            if (prevStep.edge.type == EdgeType.WALKING) {
                val destinationName = if (current == "DESTINATION") "tu destino" else (graph[current]?.stopName ?: "parada")
                
                rawSteps.add(0, WalkStep(
                    durationMinutes = (prevStep.edge.weightSeconds / 60).toInt().coerceAtLeast(1),
                    distanceMeters = prevStep.edge.distanceMeters.toInt(),
                    instructions = "Camina hacia $destinationName"
                ))
            } else {
                val lastAdded = rawSteps.firstOrNull()
                val prevNodeName = graph[prevStep.previousNodeId]?.stopName ?: "..."
                val currentNodeName = graph[current]?.stopName ?: "?"

                if (lastAdded is BusStep && lastAdded.routeName == prevStep.edge.routeName && lastAdded.direction == prevStep.edge.direction) {
                    rawSteps[0] = lastAdded.copy(
                        durationMinutes = lastAdded.durationMinutes + (prevStep.edge.weightSeconds / 60).toInt(),
                        boardStopName = prevNodeName,
                        stopsCount = lastAdded.stopsCount + 1
                    )
                } else {
                     rawSteps.add(0, BusStep(
                        durationMinutes = (prevStep.edge.weightSeconds / 60).toInt().coerceAtLeast(1),
                        routeName = prevStep.edge.routeName ?: "Bus",
                        routeColor = getColorForRoute(prevStep.edge.routeName ?: ""),
                        boardStopName = prevNodeName,
                        alightStopName = currentNodeName,
                        stopsCount = 1,
                        direction = prevStep.edge.direction
                    ))
                }
            }

            if (prevStep.isStartWalk) break
            current = prevStep.previousNodeId ?: break
        }

        return TripItinerary(
            totalDurationMinutes = (totalTimeSeconds / 60).toInt(),
            startTime = "Ahora",
            endTime = "+${(totalTimeSeconds / 60).toInt()} min",
            steps = rawSteps
        )
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
    
    private fun getColorForRoute(routeName: String): Color {
        return when(routeName) {
            "1" -> Color(0xFF4CAF50)
            "2" -> Color(0xFF2196F3)
            "3" -> Color(0xFFFF9800)
            "17" -> Color(0xFF9C27B0)
            else -> Color.Gray
        }
    }

    data class PreviousStep(
        val previousNodeId: String?, 
        val edge: Edge,
        val isStartWalk: Boolean = false
    )
    
    data class QueueNode(val id: String, val time: Double) : Comparable<QueueNode> {
        override fun compareTo(other: QueueNode): Int = time.compareTo(other.time)
    }
}
