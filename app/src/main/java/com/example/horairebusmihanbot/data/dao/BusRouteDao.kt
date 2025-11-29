package com.example.horairebusmihanbot.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entity.BusRoute

@Dao
interface BusRouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routes: List<BusRoute>)

    @Query("DELETE FROM bus_route")
    suspend fun deleteAll()

    @Query("SELECT * FROM bus_route WHERE route_type = 3 ORDER BY route_short_name ASC")
    fun getAll(): LiveData<List<BusRoute>>
}