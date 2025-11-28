package com.example.mirutadigital

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.mirutadigital.data.local.UserPreferences
import com.example.mirutadigital.data.local.database.AppDatabase
import com.example.mirutadigital.data.network.NetworkProvider
import com.example.mirutadigital.data.repository.RouteRepository
import com.example.mirutadigital.data.repository.UserIdProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit

/**
 * Clase Application personalizada para inicializar y mantener
 * la base de datos y el repositorio como singletons para los mv
 */
class MiRutaApplication : Application() {

    // CoroutineScope para toda la app, este para la sincronizacion
    private val applicationScope = CoroutineScope(Dispatchers.IO)

    val userIdProvider: UserIdProvider by lazy {
        UserIdProvider(this)
    }

    // inicializacion de la base de datos
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    // inicializacion de preferencias del usuario
    val userPreferences: UserPreferences by lazy {
        UserPreferences(this)
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        triggerBackgroundSync()
    }

    /**
     * inicia la corrutina para sincronizar los datos en segundo plano
     * sin bloquear el arranque de la app y que se atrase la ui
     */
    private fun triggerBackgroundSync() {
        applicationScope.launch {
            val prefs = getSharedPreferences("SyncPreferences", MODE_PRIVATE)
            val lastSyncTimestamp = prefs.getLong("last_sync_timestamp", 0L)
            val oneHourInMillis = 60 * 60 * 1000 // una hoar

            if (System.currentTimeMillis() - lastSyncTimestamp > oneHourInMillis) {
                Log.d("MiRutaApplication", "Iniciando sincronización en segundo plano...")
                try {
                    repository.synchronizeDatabase()
                    prefs.edit { putLong("last_sync_timestamp", System.currentTimeMillis()) }
                    Log.d("MiRutaApplication", "Sincronización en segundo plano completada exitosamente.")
                } catch (e: Exception) {
                    Log.e("MiRutaApplication", "Error durante la sincronización en segundo plano", e)
                }
            } else {
                // ya estan actualizados
            }
        }
    }

    // singleton del repositorio
    val repository: RouteRepository by lazy {
        RouteRepository(
            routeDao = database.routeDao(),
            favoriteRouteDao = database.favoriteRouteDao(),
            routeHistoryDao = database.routeHistoryDao(),
            routeRatingDao = database.routeRatingDao(),
            firestore = firestore,
            googleApiService = NetworkProvider.googleApiService,
            userIdProvider = userIdProvider
        )
    }
}
