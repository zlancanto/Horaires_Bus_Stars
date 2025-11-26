package com.example.horairebusmihanbot.data.impl

import com.example.horairebusmihanbot.data.dao.StopTimeDao
import com.example.horairebusmihanbot.data.entity.StopTime

class StopTimeImpl(private val dao: StopTimeDao) {
    suspend fun insertAll(stopTimes: List<StopTime>) = dao.insertAll(stopTimes)
    suspend fun deleteAll() = dao.deleteAll()
}