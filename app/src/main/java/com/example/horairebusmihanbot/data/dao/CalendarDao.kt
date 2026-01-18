package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entities.Calendar

@Dao
interface CalendarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendars(calendars: List<Calendar>)

    @Query("DELETE FROM calendar")
    suspend fun deleteAllCalendars()
}