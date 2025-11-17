package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entity.*
import kotlinx.coroutines.flow.Flow
@Dao
interface StopDao {
    @Query("SELECT * FROM stop")
    fun getAll(): Flow<List<Stop>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stops: List<Stop>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stop: Stop)

    @Query("DELETE FROM stop")
    suspend fun clear()
}
