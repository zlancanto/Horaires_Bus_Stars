package com.example.horairebusmihanbot.data.repository

import com.example.horairebusmihanbot.data.dao.BusRouteDao
import com.example.horairebusmihanbot.data.entity.BusRoute

class BusRouteRepository(private val dao: BusRouteDao) {
    suspend fun insertAll(routes: List<BusRoute>) = dao.insertAll(routes)
    suspend fun deleteAll() = dao.deleteAll()
}