package com.example.horairebusmihanbot.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entities.BusRoute

@Dao
interface BusRouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<BusRoute>)

    @Query("SELECT * FROM bus_route")
    fun getAllRoutes(): LiveData<List<BusRoute>>

    @Query("DELETE FROM bus_route")
    suspend fun deleteAllRoutes()
}