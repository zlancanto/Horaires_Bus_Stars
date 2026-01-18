package com.example.horairebusmihanbot.repository

import com.example.horairebusmihanbot.data.AppDatabase
import com.example.horairebusmihanbot.data.entities.Trip

class TripRepository(private val db: AppDatabase) {
    private val tripDao = db.tripDao()

    suspend fun insertTrips(trips: List<Trip>) = tripDao.insertTrips(trips)

    suspend fun deleteAllTrips() = tripDao.deleteAllTrips()

    suspend fun getTripDetails(tripId: String, stopSequence: Int) = tripDao.getTripDetails(tripId, stopSequence)

}