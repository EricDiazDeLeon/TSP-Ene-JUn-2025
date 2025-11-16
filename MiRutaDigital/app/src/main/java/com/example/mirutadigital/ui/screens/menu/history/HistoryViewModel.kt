package com.example.mirutadigital.ui.screens.menu.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirutadigital.MiRutaApplication
import com.example.mirutadigital.data.local.entities.RouteHistoryEntity
import com.example.mirutadigital.ui.util.SnackbarManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val historyList: List<RouteHistoryEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val saveHistoryEnabled: Boolean = true
) {
    val filteredHistory: List<RouteHistoryEntity>
        get() {
            return if (searchQuery.isBlank()) {
                historyList
            } else {
                historyList.filter {
                    it.routeName.contains(searchQuery, ignoreCase = true)
                }
            }
        }

    val historyCount: Int
        get() = historyList.size
}

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MiRutaApplication).repository
    private val userPreferences = (application as MiRutaApplication).userPreferences

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // cargar preferencia de guardado
            val saveEnabled = userPreferences.getSaveHistoryEnabled()

            // observar historial
            repository.getAllHistory().collect { history ->
                _uiState.update {
                    it.copy(
                        historyList = history,
                        isLoading = false,
                        saveHistoryEnabled = saveEnabled
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSaveHistoryEnabled() {
        val newValue = !_uiState.value.saveHistoryEnabled
        userPreferences.setSaveHistoryEnabled(newValue)
        _uiState.update { it.copy(saveHistoryEnabled = newValue) }

        val message = if (newValue) {
            "Historial activado"
        } else {
            "Historial desactivado"
        }
        viewModelScope.launch { SnackbarManager.showMessage(message) }
    }

    fun deleteHistoryItem(historyId: Long) {
        viewModelScope.launch {
            repository.removeFromHistory(historyId)
            SnackbarManager.showMessage("Eliminado del historial")
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch {
            repository.deleteAllHistory()
            SnackbarManager.showMessage("Historial eliminado completamente")
        }
    }
}