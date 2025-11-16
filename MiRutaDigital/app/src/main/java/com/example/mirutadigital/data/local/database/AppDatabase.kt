package com.example.mirutadigital.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mirutadigital.data.local.dao.FavoriteRouteDao
import com.example.mirutadigital.data.local.dao.RouteDao
import com.example.mirutadigital.data.local.dao.RouteHistoryDao
import com.example.mirutadigital.data.local.entities.FavoriteRouteEntity
import com.example.mirutadigital.data.local.entities.JourneyEntity
import com.example.mirutadigital.data.local.entities.JourneyStopCrossRef
import com.example.mirutadigital.data.local.entities.RouteEntity
import com.example.mirutadigital.data.local.entities.RouteHistoryEntity
import com.example.mirutadigital.data.local.entities.StopEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

/**
 * Base de datos principal de la aplicacion
 */
@Database(
    entities = [
        StopEntity::class,
        RouteEntity::class,
        JourneyEntity::class,
        JourneyStopCrossRef::class,
        FavoriteRouteEntity::class,
        RouteHistoryEntity::class,
        com.example.mirutadigital.data.local.entities.RouteRatingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun routeDao(): RouteDao
    abstract fun favoriteRouteDao(): FavoriteRouteDao
    abstract fun routeHistoryDao(): RouteHistoryDao
    abstract fun routeRatingDao(): com.example.mirutadigital.data.local.dao.RouteRatingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "miruta_database"
                )
                    .fallbackToDestructiveMigration() // para desarrollo
                    //.addCallback(AppDatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Callback para llenar la BD desde los JSON en /assets (ejemplos de datos)
         */
        private class AppDatabaseCallback(
            private val context: Context
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(context, database.routeDao())
                    }
                }
            }
        }

        /**
         * Funcion que lee los JSON y los "deconstruye" en las
         * entidades de la base de datos (RouteEntity, StopEntity, JourneyEntity, ...)
         */
        suspend fun populateDatabase(context: Context, routeDao: RouteDao) {
            val gson = Gson()

            // cargar y parsear paradas (Stops)
            val stopJsonType = object : TypeToken<List<StopJson>>() {}.type
            val stopReader = InputStreamReader(context.assets.open("sample_stops.json"))
            val stopsJson: List<StopJson> = gson.fromJson(stopReader, stopJsonType)

            // mapear de StopJson a StopEntity
            val stopEntities = stopsJson.map { stopJson ->
                StopEntity(
                    stopId = stopJson.id,
                    name = stopJson.name,
                    latitude = stopJson.coordinates.latitude,
                    longitude = stopJson.coordinates.longitude
                )
            }
            // insertar paradas en la BD
            routeDao.insertStops(stopEntities)


            // cargar y parsear rutas (Routes)
            val routeJsonType = object : TypeToken<List<RouteJson>>() {}.type
            val routeReader = InputStreamReader(context.assets.open("sample_routes.json"))
            val routesJson: List<RouteJson> = gson.fromJson(routeReader, routeJsonType)

            // listas para guardar las entidades "aplanadas"
            val routeEntities = mutableListOf<RouteEntity>()
            val journeyEntities = mutableListOf<JourneyEntity>()
            val crossRefEntities = mutableListOf<JourneyStopCrossRef>()

            // iterar sobre cada ruta del JSON
            for (routeJson in routesJson) {
                // crear RouteEntity
                routeEntities.add(
                    RouteEntity(
                        routeId = routeJson.id,
                        name = routeJson.name,
                        departureInterval = routeJson.departureInterval,
                        windshieldLabel = routeJson.detail?.windshieldLabel ?: "N/A",
                        colors = routeJson.detail?.colors ?: "Sin Especificar",
                        price = routeJson.detail?.price ?: 0.0,
                        lastUpdate = System.currentTimeMillis()
                    )
                )

                // crear JourneyEntity para ida (OUTBOUND)
                val outboundJourneyId = "${routeJson.id}_OUTBOUND"
                journeyEntities.add(
                    JourneyEntity(
                        journeyId = outboundJourneyId,
                        routeOwnerId = routeJson.id,
                        journeyType = "OUTBOUND",
                        firstDeparture = routeJson.outboundJourney.firstDeparture,
                        lastDeparture = routeJson.outboundJourney.lastDeparture,
                        encodedPolyline = routeJson.outboundJourney.encodedPolyline
                    )
                )

                // crear JourneyStopCrossRef para ida
                routeJson.outboundJourney.stops.forEachIndexed { index, stopIdJson ->
                    crossRefEntities.add(
                        JourneyStopCrossRef(
                            journeyId = outboundJourneyId,
                            stopId = stopIdJson.id,
                            stopOrder = index
                        )
                    )
                }

                // crear JourneyEntity para vuelta (INBOUND)
                val inboundJourneyId = "${routeJson.id}_INBOUND"
                journeyEntities.add(
                    JourneyEntity(
                        journeyId = inboundJourneyId,
                        routeOwnerId = routeJson.id,
                        journeyType = "INBOUND",
                        firstDeparture = routeJson.inboundJourney.firstDeparture,
                        lastDeparture = routeJson.inboundJourney.lastDeparture,
                        encodedPolyline = routeJson.inboundJourney.encodedPolyline
                    )
                )

                // crear JourneyStopCrossRef para vuelta (INBOUND)
                routeJson.inboundJourney.stops.forEachIndexed { index, stopIdJson ->
                    crossRefEntities.add(
                        JourneyStopCrossRef(
                            journeyId = inboundJourneyId,
                            stopId = stopIdJson.id,
                            stopOrder = index
                        )
                    )
                }
            }

            // insertar todas las entidades de rutas en la base de datos
            routeDao.insertRoutes(routeEntities)
            routeDao.insertJourneys(journeyEntities)
            routeDao.insertJourneyStopCrossRefs(crossRefEntities)
        }
    }
}

// --- modelos de datos temporales (POJOs) para parsear los JSON ---
// pd. talvez desapues moverlos a otro archivo separado
private data class CoordinatesJson(
    val latitude: Double,
    val longitude: Double
)

private data class StopJson(
    val id: String,
    val name: String,
    val coordinates: CoordinatesJson
)

private data class StopIdJson(
    val id: String
)

private data class JourneyJson(
    val firstDeparture: String,
    val lastDeparture: String,
    val encodedPolyline: String?,
    val stops: List<StopIdJson>
)

private data class RouteDetailJson(
    val windshieldLabel: String,
    val colors: String,
    val price: Double
)

private data class RouteJson(
    val id: String,
    val name: String,
    val departureInterval: Int,
    val detail: RouteDetailJson?,
    val outboundJourney: JourneyJson,
    val inboundJourney: JourneyJson
)
