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

/**
 * DAO principal para manejar las inserciones y consultas complejas
 */
@Dao
interface RouteDao {

    @Transaction
    suspend fun sincronizarCompleto(
        stops: List<StopEntity>,
        routes: List<RouteEntity>,
        journeys: List<JourneyEntity>,
        crossRefs: List<JourneyStopCrossRef>
    ) {
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
     * Obtiene todas las paradas con los trayectos (de todas las rutas)
     * que pasan por ellas
     */
    @Transaction
    @Query("SELECT * FROM stops")
    suspend fun getAllStopsWithJourneys(): List<StopWithJourneys>

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
}