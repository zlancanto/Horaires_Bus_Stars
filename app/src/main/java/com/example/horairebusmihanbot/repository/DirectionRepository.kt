package com.example.horairebusmihanbot.repository

import com.example.horairebusmihanbot.data.AppDatabase

class DirectionRepository(private val db: AppDatabase) {
    private val directionDao = db.directionDao()

    suspend fun getDirections(routeId: String) = directionDao.getDirections(routeId)
}