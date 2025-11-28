package com.example.mirutadigital.ui.screens.share

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.MiRutaApplication
import com.example.mirutadigital.data.model.ui.RoutesInfo
import com.example.mirutadigital.data.repository.UserIdProvider
import com.example.mirutadigital.ui.util.ShareManager
import com.example.mirutadigital.ui.util.SnackbarManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShareUiState(
    val routes: List<RoutesInfo> = emptyList(),
    val favoriteRouteIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val expandedRouteId: String? = null,
    val showOnlyFavorites: Boolean = false,
    val showJourneyDialog: Boolean = false
) {
    val filteredRoutes: List<RoutesInfo>
        get() = routes.filter { route ->
            val matchesSearch = route.name.contains(searchQuery, ignoreCase = true)
            val matchesFavorite = if (showOnlyFavorites) favoriteRouteIds.contains(route.id) else true
            matchesSearch && matchesFavorite
        }
}

class ShareViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiRutaApplication).repository
    private val userPreferences = (application as MiRutaApplication).userPreferences
    private val userIdProvider: UserIdProvider = (application as MiRutaApplication).userIdProvider


    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    private val _allRoutes = MutableStateFlow<List<RoutesInfo>>(emptyList())
    private val _favoriteRouteIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            //repository.synchronizeDatabase()
            val routes = repository.getGeneralRoutesInfo()
            _allRoutes.value = routes

            combine(
                _allRoutes,
                repository.getAllFavorites()
            ) { allRoutes, favorites ->
                val favIds = favorites.map { it.routeId }.toSet()
                _favoriteRouteIds.value = favIds
                _uiState.update {
                    it.copy(
                        routes = allRoutes,
                        favoriteRouteIds = favIds,
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleShowOnlyFavorites() {
        _uiState.update { it.copy(showOnlyFavorites = !it.showOnlyFavorites) }
    }

    fun toggleExpand(routeId: String) {
        _uiState.update {
            it.copy(expandedRouteId = if (it.expandedRouteId == routeId) null else routeId)
        }
    }

    fun toggleFavorite(routeId: String) {
        viewModelScope.launch {
            val isFavorite = routeId in _uiState.value.favoriteRouteIds
            repository.toggleFavorite(routeId, isFavorite)
        }
    }

    fun isFavorite(routeId: String): Boolean {
        return routeId in _uiState.value.favoriteRouteIds
    }

    fun onShareClick() {
        _uiState.update { it.copy(showJourneyDialog = true) }
    }

    fun onDismissJourneyDialog() {
        _uiState.update { it.copy(showJourneyDialog = false) }
    }

    /**
     * Se llama desde el dialogo al confirmar el trayecto ida o vuelta
     */
    fun onConfirmShare(location: Location, journeyType: String) {
        val routeId = _uiState.value.expandedRouteId ?: return
        val routeName = _uiState.value.routes.find { it.id == routeId }?.name ?: "Ruta"
        val userId = userIdProvider.getUserId()

        viewModelScope.launch {
            repository.startShare(userId, routeId, journeyType, location)

            if (userPreferences.getSaveHistoryEnabled()) {
                repository.addToHistory(routeId, routeName)
            }

            ShareManager.startSharing(routeId)
            SnackbarManager.showMessage("¡Compartiendo $routeName!")

            _uiState.update { it.copy(showJourneyDialog = false) }
        }
    }

    fun stopShare() {
        val routeId = ShareManager.currentSharedRouteId.value ?: return
        val userId = userIdProvider.getUserId()

        viewModelScope.launch {
            repository.stopShare(userId, routeId)
            ShareManager.stopSharing()
            SnackbarManager.showMessage("Dejaste de compartir")
        }
    }

    /**
     * NUEVO: Se llama cuando el ViewModel es destruido.
     * Intenta detener la compartición si el usuario cierra la app.
     */
    override fun onCleared() {
        super.onCleared()
        if (ShareManager.currentSharedRouteId.value != null) {
            stopShare()
        }
    }
}
