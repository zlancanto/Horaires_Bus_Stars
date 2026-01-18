package com.example.horairebusmihanbot.repository

import com.example.horairebusmihanbot.data.AppDatabase
import com.example.horairebusmihanbot.data.entities.Calendar

class CalendarRepository(private val db: AppDatabase) {
    private val calendarDao = db.calendarDao()

    suspend fun insertCalendars(calendars: List<Calendar>) = calendarDao.insertCalendars(calendars)

    suspend fun deleteAllCalendars() = calendarDao.deleteAllCalendars()

}