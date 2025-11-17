package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entity.*
import kotlinx.coroutines.flow.Flow
@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar")
    fun getAll(): Flow<List<Calendar>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Calendar>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calendar: Calendar)

    @Query("DELETE FROM calendar")
    suspend fun clear()
}
