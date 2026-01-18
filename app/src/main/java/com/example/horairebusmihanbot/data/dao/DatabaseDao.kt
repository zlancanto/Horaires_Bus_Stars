package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface DatabaseDao {
    @Query("SELECT (SELECT COUNT(*) FROM stop) <= 20")
    suspend fun isDatabaseEmpty(): Boolean
}