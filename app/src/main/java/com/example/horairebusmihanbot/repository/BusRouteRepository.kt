package com.example.horairebusmihanbot.repository

import com.example.horairebusmihanbot.data.entity.BusRoute
import kotlinx.coroutines.flow.Flow

interface BusRouteRepository {
    suspend fun insertAll(routes: List<BusRoute>)
    suspend fun deleteAll()
    fun getAllAsFlow(): Flow<List<BusRoute>>
    suspend fun getDirectionsByRoute(routeId: String): List<String>
}