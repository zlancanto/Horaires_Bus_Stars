package com.example.horairebusmihanbot.data.repository

import com.example.horairebusmihanbot.data.dao.*
import com.example.horairebusmihanbot.data.entity.*
import kotlinx.coroutines.flow.Flow

class CalendarRepository(private val dao: CalendarDao) {

    val allCalendars: Flow<List<Calendar>> = dao.getAll()

    suspend fun insert(calendar: Calendar) = dao.insert(calendar)
    suspend fun insertAll(list: List<Calendar>) = dao.insertAll(list)
    suspend fun clear() = dao.clear()
}
