package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StopTimeDao {
    @Query("SELECT * FROM stop_time")
    fun getAll(): Flow<List<StopTime>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<StopTime>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stopTime: StopTime)

    @Query("DELETE FROM stop_time")
    suspend fun clear()
}

