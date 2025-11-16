package com.example.mirutadigital.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Clase para manejar las preferencias del usuario
 */
class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "miruta_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_SAVE_HISTORY = "save_route_history"
    }

    /**
     * Obtiene si el usuario quiere guardar el historial de rutas
     */
    fun getSaveHistoryEnabled(): Boolean {
        return prefs.getBoolean(KEY_SAVE_HISTORY, true)
    }

    /**
     * Establece si el usuario quiere guardar el historial de rutas
     */
    fun setSaveHistoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SAVE_HISTORY, enabled).apply()
    }
}