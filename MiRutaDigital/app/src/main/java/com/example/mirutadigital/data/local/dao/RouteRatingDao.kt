package com.example.mirutadigital.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mirutadigital.data.local.entities.RouteRatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteRatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: RouteRatingEntity): Long

    @Query("SELECT * FROM route_ratings WHERE pendingSync = 1 ORDER BY createdAt ASC")
    suspend fun getPendingRatings(): List<RouteRatingEntity>

    @Query("UPDATE route_ratings SET pendingSync = 0 WHERE id = :id")
    suspend fun markRatingSynced(id: Long)

    @Query("SELECT * FROM route_ratings WHERE routeId = :routeId ORDER BY createdAt DESC")
    fun getRatingsForRoute(routeId: String): Flow<List<RouteRatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ratings: List<RouteRatingEntity>)

    @Query("DELETE FROM route_ratings WHERE routeId = :routeId AND pendingSync = 0")
    suspend fun deleteSyncedRatingsForRoute(routeId: String)

    @Transaction
    suspend fun syncRatings(routeId: String, ratings: List<RouteRatingEntity>) {
        deleteSyncedRatingsForRoute(routeId)
        insertAll(ratings)
    }
}