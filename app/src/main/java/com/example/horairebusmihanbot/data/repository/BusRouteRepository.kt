package com.example.horairebusmihanbot.data.repository

import com.example.horairebusmihanbot.data.dao.*
import com.example.horairebusmihanbot.data.entity.*
import kotlinx.coroutines.flow.Flow

class BusRouteRepository(private val dao: BusRouteDao) {

    val allRoutes: Flow<List<BusRoute>> = dao.getAll()

    suspend fun insert(route: BusRoute) = dao.insert(route)
    suspend fun insertAll(list: List<BusRoute>) = dao.insertAll(list)
    suspend fun clear() = dao.clear()
}
