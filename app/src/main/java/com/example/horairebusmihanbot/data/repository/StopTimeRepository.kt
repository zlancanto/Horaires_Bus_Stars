package com.example.horairebusmihanbot.data.repository

import com.example.horairebusmihanbot.data.dao.*
import com.example.horairebusmihanbot.data.entity.*
import kotlinx.coroutines.flow.Flow

class StopTimeRepository(private val dao: StopTimeDao) {

    val allStopTimes: Flow<List<StopTime>> = dao.getAll()

    suspend fun insert(stopTime: StopTime) = dao.insert(stopTime)
    suspend fun insertAll(list: List<StopTime>) = dao.insertAll(list)
    suspend fun clear() = dao.clear()
}
