package com.example.horairebusmihanbot.data.repository

import com.example.horairebusmihanbot.data.dao.StopTimeDao
import com.example.horairebusmihanbot.data.entity.StopTime

class StopTimeRepository(private val dao: StopTimeDao) {
    suspend fun insertAll(stopTimes: List<StopTime>) = dao.insertAll(stopTimes)
    suspend fun deleteAll() = dao.deleteAll()
}