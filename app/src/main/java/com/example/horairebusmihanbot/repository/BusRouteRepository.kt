package com.example.horairebusmihanbot.repository

import androidx.lifecycle.LiveData
import com.example.horairebusmihanbot.data.entity.BusRoute

interface BusRouteRepository {
    suspend fun insertAll(routes: List<BusRoute>)
    suspend fun deleteAll()
    fun getAll(): LiveData<List<BusRoute>>
    suspend fun getDirectionsByRoute(routeId: String): List<String>
}