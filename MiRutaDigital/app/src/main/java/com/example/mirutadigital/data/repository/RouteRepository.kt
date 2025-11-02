package com.example.mirutadigital.data.repository

import com.example.mirutadigital.data.model.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class RouteRepository {
    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: Flow<List<Route>> = _routes
    
    // Obtener solo rutas favoritas
    val favoriteRoutes: Flow<List<Route>> = _routes.map { routes ->
        routes.filter { it.isFavorite }
    }
    
    // Obtener IDs de rutas favoritas
    suspend fun getFavoriteRouteIds(): List<String> {
        return _routes.value.filter { it.isFavorite }.map { it.id }
    }
    
    // Alternar estado de favorito
    suspend fun toggleFavoriteRoute(routeId: String) {
        val currentRoutes = _routes.value
        val updatedRoutes = currentRoutes.map { route ->
            if (route.id == routeId) {
                val newFavoriteStatus = !route.isFavorite
                route.copy(isFavorite = newFavoriteStatus)
            } else {
                route
            }
        }
        _routes.value = updatedRoutes
    }
    
    // Actualizar estado de favorito de una ruta
    suspend fun setFavoriteRoute(routeId: String, isFavorite: Boolean): Boolean {
        return try {
            // Actualizar en memoria
            _routes.update { currentRoutes ->
                currentRoutes.map { route ->
                    if (route.id == routeId) {
                        route.copy(isFavorite = isFavorite)
                    } else {
                        route
                    }
                }
            }
            true
        } catch (e: Exception) {
            // Manejar excepción almacenamiento no disponible (no tuve idea de que poner aqui.)
            false
        }
    }
    
    // Método para cargar rutas
    suspend fun loadRoutes(routes: List<Route>) {
        // Mantener el estado de favoritos para las rutas existentes
        val favoriteIds = _routes.value.filter { it.isFavorite }.map { it.id }
        
        // Actualizar rutas con estado de favorito
        val updatedRoutes = routes.map { route ->
            route.copy(isFavorite = favoriteIds.contains(route.id))
        }
        
        _routes.value = updatedRoutes
    }
    
    // Método para obtener una ruta por ID
    fun getRouteById(routeId: String): Route? {
        return _routes.value.find { it.id == routeId }
    }
}