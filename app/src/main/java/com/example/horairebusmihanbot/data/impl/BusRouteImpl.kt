package com.example.horairebusmihanbot.data.impl

import androidx.lifecycle.LiveData
import com.example.horairebusmihanbot.data.dao.BusRouteDao
import com.example.horairebusmihanbot.data.dao.TripDao
import com.example.horairebusmihanbot.data.entity.BusRoute
import com.example.horairebusmihanbot.exception.requireNonNull
import com.example.horairebusmihanbot.repository.BusRouteRepository
import kotlinx.coroutines.flow.Flow

class BusRouteImpl private constructor(
    private val busRouteDao: BusRouteDao?,
    private val tripDao: TripDao?
) : BusRouteRepository {

    override
    suspend fun insertAll(routes: List<BusRoute>) {
        val busRouteDao = requireNonNull(busRouteDao, "BusRouteDao cannot be null")
        return busRouteDao.insertAll(routes)
    }

    override
    suspend fun deleteAll() {
        val busRouteDao = requireNonNull(busRouteDao, "BusRouteDao cannot be null")
        busRouteDao.deleteAll()
    }

    override
    fun getAllAsFlow(): Flow<List<BusRoute>> {
        val busRouteDao = requireNonNull(busRouteDao, "BusRouteDao cannot be null")
        return busRouteDao.getAllAsFlow()
    }

    override
    suspend fun getDirectionsByRoute(routeId: String): List<String> {
        val tripDao = requireNonNull(tripDao, "TripDao cannot be null")
        return tripDao.getDirectionsByRouteId(routeId)
    }

    class Builder {
        private var busRouteDao: BusRouteDao? = null
        private var tripDao: TripDao? = null

        fun busRouteDao(busRouteDao: BusRouteDao) = apply { this.busRouteDao = busRouteDao }

        fun tripDao(tripDao: TripDao) = apply { this.tripDao = tripDao }

        fun build() = BusRouteImpl(busRouteDao, tripDao)
    }
}