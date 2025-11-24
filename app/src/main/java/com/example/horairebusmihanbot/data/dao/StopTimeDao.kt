package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entity.StopTime

@Dao
interface StopTimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routes: List<StopTime>)
    @Query("DELETE FROM stop_time")
    suspend fun deleteAll()
}