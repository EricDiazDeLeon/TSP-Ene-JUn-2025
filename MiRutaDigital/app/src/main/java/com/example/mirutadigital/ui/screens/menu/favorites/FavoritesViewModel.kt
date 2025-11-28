package com.example.mirutadigital.ui.screens.menu.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.MiRutaApplication
import com.example.mirutadigital.data.model.ui.RoutesInfo
import com.example.mirutadigital.ui.util.SnackbarManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val allRoutes: List<RoutesInfo> = emptyList(),
    val favoriteRouteIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val showOnlyFavorites: Boolean = false
) {
    val filteredRoutes: List<RoutesInfo>
        get() {
            var result = allRoutes

            // filtrar solo favoritos si esta activado
            if (showOnlyFavorites) {
                result = result.filter { it.id in favoriteRouteIds }
            }

            // filtrar por busqueda
            if (searchQuery.isNotBlank()) {
                result = result.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }

            return result
        }

    val favoritesCount: Int
        get() = favoriteRouteIds.size
}

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiRutaApplication).repository

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val routes = repository.getGeneralRoutesInfo()

            repository.getAllFavorites().collect { favorites ->
                val favoriteIds = favorites.map { it.routeId }.toSet()
                _uiState.update {
                    it.copy(
                        allRoutes = routes,
                        favoriteRouteIds = favoriteIds,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleShowOnlyFavorites() {
        _uiState.update { it.copy(showOnlyFavorites = !it.showOnlyFavorites) }
    }

    fun toggleFavorite(routeId: String) {
        viewModelScope.launch {
            val isFavorite = routeId in _uiState.value.favoriteRouteIds
            repository.toggleFavorite(routeId, isFavorite)

            val message = if (isFavorite) {
                "Eliminada de favoritos"
            } else {
                "Agregada a favoritos"
            }
            SnackbarManager.showMessage(message)
        }
    }

    fun deleteAllFavorites() {
        viewModelScope.launch {
            repository.deleteAllFavorites()
            SnackbarManager.showMessage("Todos los favoritos eliminados")
        }
    }
}