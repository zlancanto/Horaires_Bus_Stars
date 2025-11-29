package com.example.horairebusmihanbot.data.impl

import com.example.horairebusmihanbot.data.dao.StopDao
import com.example.horairebusmihanbot.data.entity.Stop

class StopImpl(private val dao: StopDao) {
    suspend fun insertAll(stops: List<Stop>) = dao.insertAll(stops)
    suspend fun deleteAll() = dao.deleteAll()
}