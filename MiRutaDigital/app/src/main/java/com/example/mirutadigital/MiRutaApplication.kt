package com.example.mirutadigital

import android.app.Application
import com.example.mirutadigital.data.local.UserPreferences
import com.example.mirutadigital.data.local.database.AppDatabase
import com.example.mirutadigital.data.network.NetworkProvider
import com.example.mirutadigital.data.repository.RouteRepository
import com.example.mirutadigital.data.repository.UserIdProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

/**
 * Clase Application personalizada para inicializar y mantener
 * la base de datos y el repositorio como singletons para los mv
 * (Para mantener la persistencia)
 */
class MiRutaApplication : Application() {

    val userIdProvider: UserIdProvider by lazy {
        UserIdProvider(this)
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseAuth.getInstance().signInAnonymously()
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

    // MODIFICADO: Inyectamos el userIdProvider al repositorio
    val repository: RouteRepository by lazy {
        RouteRepository(
            routeDao = database.routeDao(),
            favoriteRouteDao = database.favoriteRouteDao(),
            routeHistoryDao = database.routeHistoryDao(),
            routeRatingDao = database.routeRatingDao(),
            firestore = firestore,
            googleApiService = NetworkProvider.googleApiService,
            userIdProvider = userIdProvider,
            appContext = this
        )
    }
}
