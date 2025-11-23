package com.example.horairebusmihanbot.data.repository

import com.example.horairebusmihanbot.data.dao.StopDao
import com.example.horairebusmihanbot.data.entity.Stop

class StopRepository(private val dao: StopDao) {
    suspend fun insertAll(stops: List<Stop>) = dao.insertAll(stops)
}