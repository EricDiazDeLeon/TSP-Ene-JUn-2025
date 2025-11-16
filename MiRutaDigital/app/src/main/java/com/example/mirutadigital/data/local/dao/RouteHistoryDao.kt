package com.example.mirutadigital.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mirutadigital.data.local.entities.RouteHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para manejar el historial de rutas compartidas
 */
@Dao
interface RouteHistoryDao {

    /**
     * Inserta una ruta en el historial
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: RouteHistoryEntity)

    /**
     * Elimina una entrada del historial
     */
    @Delete
    suspend fun deleteHistory(history: RouteHistoryEntity)

    /**
     * Elimina una entrada del historial por su ID
     */
    @Query("DELETE FROM route_history WHERE id = :historyId")
    suspend fun deleteHistoryById(historyId: Long)

    /**
     * Obtiene el historial ordenado por fecha
     */
    @Query("SELECT * FROM route_history ORDER BY sharedAt DESC")
    fun getAllHistory(): Flow<List<RouteHistoryEntity>>

    /**
     * Elimina el historial
     */
    @Query("DELETE FROM route_history")
    suspend fun deleteAllHistory()

    /**
     * Obtiene la cantidad de datos en el historial
     */
    @Query("SELECT COUNT(*) FROM route_history")
    suspend fun getHistoryCount(): Int
}