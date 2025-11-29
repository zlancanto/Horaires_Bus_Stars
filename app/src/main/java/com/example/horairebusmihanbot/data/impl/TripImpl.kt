package com.example.horairebusmihanbot.data.impl

import com.example.horairebusmihanbot.data.dao.TripDao
import com.example.horairebusmihanbot.data.entity.Trip

class TripImpl(private val dao: TripDao) {
    suspend fun insertAll(voyages: List<Trip>) = dao.insertAll(voyages)
    suspend fun deleteAll() = dao.deleteAll()
}