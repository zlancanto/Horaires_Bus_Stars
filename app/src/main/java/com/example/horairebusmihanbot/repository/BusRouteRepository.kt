package com.example.horairebusmihanbot.repository

import com.example.horairebusmihanbot.data.AppDatabase
import com.example.horairebusmihanbot.data.entities.BusRoute

class BusRouteRepository(private val db: AppDatabase) {
    private val routeDao = db.routeDao()

    suspend fun insertRoutes(routes: List<BusRoute>) = routeDao.insertRoutes(routes)

    fun getAllRoutes() = routeDao.getAllRoutes()

    suspend fun deleteAllRoutes() = routeDao.deleteAllRoutes()

}