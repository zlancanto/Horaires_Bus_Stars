package com.example.horairebusmihanbot.repository

import com.example.horairebusmihanbot.data.AppDatabase
import com.example.horairebusmihanbot.data.entities.Stop

class StopRepository(private val db: AppDatabase) {
    private val stopDao = db.stopDao()

    suspend fun insertStops(stops: List<Stop>) = stopDao.insertStops(stops)

    suspend fun getStops(routeId: String, directionId: Int) = stopDao.getStops(routeId, directionId)

    suspend fun deleteAllStops() = stopDao.deleteAllStops()
}