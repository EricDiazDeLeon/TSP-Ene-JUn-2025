package com.example.mirutadigital.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mirutadigital.data.local.entities.JourneyEntity
import com.example.mirutadigital.data.local.entities.JourneyStopCrossRef
import com.example.mirutadigital.data.local.entities.RouteEntity
import com.example.mirutadigital.data.local.entities.StopEntity
import com.example.mirutadigital.data.local.relations.RouteWithJourneys
import com.example.mirutadigital.data.local.relations.StopWithJourneys
import kotlinx.coroutines.flow.Flow

/**
 * DAO principal para manejar las inserciones y consultas complejas
 */
@Dao
interface RouteDao {

    @Transaction
    suspend fun syncchronizeAll(
        stops: List<StopEntity>,
        routes: List<RouteEntity>,
        journeys: List<JourneyEntity>,
        crossRefs: List<JourneyStopCrossRef>
    ) {
        clearStops()
        clearRoutes()
        clearJourneys()
        clearJourneyStopCrossRefs()
        
        insertStops(stops)
        insertRoutes(routes)
        insertJourneys(journeys)
        insertJourneyStopCrossRefs(crossRefs)
    }

    /**
     * metodos de Inserción para llenar la base de datos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<StopEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourneys(journeys: List<JourneyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourneyStopCrossRefs(refs: List<JourneyStopCrossRef>)

    /**
     * Metodos para borrar todos los datos de las tablas principales
     * para limpiar la BD antes de una sincronizacion.
     */
    @Query("DELETE FROM stops")
    suspend fun clearStops()

    @Query("DELETE FROM routes")
    suspend fun clearRoutes()

    @Query("DELETE FROM journeys")
    suspend fun clearJourneys()

    @Query("DELETE FROM journey_stop_cross_ref")
    suspend fun clearJourneyStopCrossRefs()

// --- METODOS DE CONSULTA ---

    /**
     * Obtiene una ruta especifica con todos sus trayectos y las paradas
     * de esos trayectos, ordenadas
     */
    @Transaction
    @Query("""
        SELECT * FROM routes 
        WHERE routeId = :routeId
    """)
    suspend fun getRouteWithJourneysById(routeId: String): RouteWithJourneys?

    /**
     * Obtiene todas las rutas con sus trayectos y paradas
     */
    @Transaction
    @Query("SELECT * FROM routes")
    suspend fun getAllRoutesWithJourneys(): List<RouteWithJourneys>

    /**
     * Obtiene todas las rutas con sus trayectos y paradas de forma reactiva
     */
    @Transaction
    @Query("SELECT * FROM routes")
    fun getAllRoutesWithJourneysFlow(): Flow<List<RouteWithJourneys>>

    /**
     * Obtiene todas las paradas con los trayectos (de todas las rutas)
     * que pasan por ellas
     */
    @Transaction
    @Query("SELECT * FROM stops")
    suspend fun getAllStopsWithJourneys(): List<StopWithJourneys>

    /**
     * Obtiene todas las paradas con los trayectos de forma reactiva
     */
    @Transaction
    @Query("SELECT * FROM stops")
    fun getAllStopsWithJourneysFlow(): Flow<List<StopWithJourneys>>

    /**
     * Obtiene la entidad de una Ruta (RouteEntity) basado en el ID
     * de uno de sus trayectos (JourneyEntity)
     * es un metodo de ayuda que se necesita en el repositorio
     */
    @Query("""
        SELECT R.* FROM routes AS R
        INNER JOIN journeys AS J ON R.routeId = J.routeOwnerId
        WHERE J.journeyId = :journeyId
    """)
    suspend fun getRouteByJourneyId(journeyId: String): RouteEntity?

    /**
     * Obtiene todas las paradas (ordenadas) de un trayecto especifico
     * es un metodo de ayuda que se necesita en el repositorio
     */
    @Query("""
        SELECT S.* FROM stops AS S
        INNER JOIN journey_stop_cross_ref AS Ref ON S.stopId = Ref.stopId
        WHERE Ref.journeyId = :journeyId
        ORDER BY Ref.stopOrder ASC
    """)
    suspend fun getStopsForJourney(journeyId: String): List<StopEntity>


    /**
     * Obtiene todas las rutas que pasan por una parada especifica
     */
    @Transaction
    @Query("""
        SELECT R.* FROM routes AS R
        INNER JOIN journeys AS J ON R.routeId = J.routeOwnerId
        INNER JOIN journey_stop_cross_ref AS Ref ON J.journeyId = Ref.journeyId
        WHERE Ref.stopId = :stopId
        GROUP BY R.routeId
    """)
    suspend fun getRoutesByStopId(stopId: String): List<RouteWithJourneys>

    @Query("SELECT * FROM journey_stop_cross_ref")
    suspend fun getAllJourneyStopCrossRefs(): List<JourneyStopCrossRef>

}
