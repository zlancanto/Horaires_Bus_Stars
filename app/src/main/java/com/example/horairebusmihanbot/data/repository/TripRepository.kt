package com.example.horairebusmihanbot.data.repository

import com.example.horairebusmihanbot.data.dao.*
import com.example.horairebusmihanbot.data.entity.*
import kotlinx.coroutines.flow.Flow

class TripRepository(private val dao: TripDao) {

    val allTrips: Flow<List<Trip>> = dao.getAll()

    suspend fun insert(trip: Trip) = dao.insert(trip)
    suspend fun insertAll(list: List<Trip>) = dao.insertAll(list)
    suspend fun clear() = dao.clear()
}
