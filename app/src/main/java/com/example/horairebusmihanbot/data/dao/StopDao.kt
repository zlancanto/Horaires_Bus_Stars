package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entity.Stop

@Dao
interface StopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routes: List<Stop>)
    @Query("DELETE FROM stop")
    suspend fun deleteAll()
}