package com.example.horairebusmihanbot.repository

import com.example.horairebusmihanbot.data.AppDatabase
import com.example.horairebusmihanbot.data.entities.StopTime

class StopTimeRepository(private val db: AppDatabase) {
    private val stopTimeDao = db.stopTimeDao()

    suspend fun insertStopTimes(times: List<StopTime>) = stopTimeDao.insertStopTimes(times)

    suspend fun deleteAllStopTimes() = stopTimeDao.deleteAllStopTimes()

    suspend fun getNextPassages(
        stopId: String,
        routeId: String,
        directionId: Int,
        currentTime: String,
        day: String
    ) = stopTimeDao.getNextPassages(stopId, routeId, directionId, currentTime, day)

}