package com.example.horairebusmihanbot.data.impl

import com.example.horairebusmihanbot.data.dao.CalendarDao
import com.example.horairebusmihanbot.data.entity.Calendar

class CalendarImpl(private val dao: CalendarDao) {
    suspend fun insertAll(dates: List<Calendar>) = dao.insertAll(dates)
    suspend fun deleteAll() = dao.deleteAll()
}