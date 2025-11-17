package com.example.horairebusmihanbot.data.repository

import com.example.horairebusmihanbot.data.dao.*
import com.example.horairebusmihanbot.data.entity.*
import kotlinx.coroutines.flow.Flow

class StopRepository(private val dao: StopDao) {

    val allStops: Flow<List<Stop>> = dao.getAll()

    suspend fun insert(stop: Stop) = dao.insert(stop)
    suspend fun insertAll(list: List<Stop>) = dao.insertAll(list)
    suspend fun clear() = dao.clear()
}
