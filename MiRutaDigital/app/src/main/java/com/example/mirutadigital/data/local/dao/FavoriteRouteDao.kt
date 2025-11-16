package com.example.mirutadigital.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mirutadigital.data.local.entities.FavoriteRouteEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para manejar las rutas favoritas del usuario
 */
@Dao
interface FavoriteRouteDao {

    /**
     * Inserta una ruta como favorita
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteRouteEntity)

    /**
     * Elimina una ruta de favoritos
     */
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteRouteEntity)

    /**
     * Elimina una ruta de favoritos por su ID
     */
    @Query("DELETE FROM favorite_routes WHERE routeId = :routeId")
    suspend fun deleteFavoriteById(routeId: String)

    /**
     * Obtiene todas las rutas favoritas
     */
    @Query("SELECT * FROM favorite_routes ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteRouteEntity>>

    /**
     * Verifica si una ruta es favorita
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_routes WHERE routeId = :routeId)")
    fun isFavorite(routeId: String): Flow<Boolean>

    /**
     * Elimina todos los favoritos
     */
    @Query("DELETE FROM favorite_routes")
    suspend fun deleteAllFavorites()

    /**
     * Obtiene la cantidad de favoritos
     */
    @Query("SELECT COUNT(*) FROM favorite_routes")
    suspend fun getFavoritesCount(): Int
}