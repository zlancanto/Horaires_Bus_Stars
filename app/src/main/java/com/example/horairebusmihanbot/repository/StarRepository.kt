package com.example.horairebusmihanbot.repository

import com.example.horairebusmihanbot.model.StarDao

class StarRepository(private val dao: StarDao) {
    val routes = dao.getAllRoutes()
    suspend fun getDirections(routeId: String) = dao.getDirections(routeId)
    suspend fun getStops(routeId: String, directionId: Int) = dao.getStops(routeId, directionId)
}