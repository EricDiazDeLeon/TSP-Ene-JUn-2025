package com.example.mirutadigital.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mirutadigital.data.local.entities.RouteRatingEntity

@Dao
interface RouteRatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: RouteRatingEntity): Long

    @Query("SELECT * FROM route_ratings WHERE pendingSync = 1 ORDER BY createdAt ASC")
    suspend fun getPendingRatings(): List<RouteRatingEntity>

    @Query("UPDATE route_ratings SET pendingSync = 0 WHERE id = :id")
    suspend fun markRatingSynced(id: Long)
}