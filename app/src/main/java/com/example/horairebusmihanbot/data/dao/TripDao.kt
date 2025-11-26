package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entity.Trip

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routes: List<Trip>)
    @Query("DELETE FROM trip")
    suspend fun deleteAll()
    @Query("SELECT trip_headsign FROM trip WHERE route_id = :routeId")
    suspend fun getDirectionsByRouteId(routeId: String): List<String>
}