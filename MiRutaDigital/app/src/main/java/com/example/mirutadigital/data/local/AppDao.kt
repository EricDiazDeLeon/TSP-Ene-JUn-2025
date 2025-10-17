package com.example.mirutadigital.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM routes")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRoutes(routes: List<RouteEntity>)

    @Query("SELECT * FROM stops")
    fun getAllStops(): Flow<List<StopEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStops(stops: List<StopEntity>)

    @Query("DELETE FROM routes")
    suspend fun deleteAllRoutes()

    @Query("DELETE FROM stops")
    suspend fun deleteAllStops()
}
