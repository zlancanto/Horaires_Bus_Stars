package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface DatabaseDao {
    @Query("SELECT (SELECT COUNT(*) FROM bus_route) == 0")
    suspend fun isDatabaseEmpty(): Boolean
}