package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entity.*
import kotlinx.coroutines.flow.Flow
@Dao
interface BusRouteDao {
    @Query("SELECT * FROM bus_route")
    fun getAll(): Flow<List<BusRoute>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routes: List<BusRoute>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: BusRoute)

    @Query("DELETE FROM bus_route")
    suspend fun clear()
}