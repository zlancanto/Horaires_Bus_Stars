package com.example.horairebusmihanbot.repository

import com.example.horairebusmihanbot.data.AppDatabase

class DatabaseRepository(private val db: AppDatabase) {
    private val databaseDao = db.databaseDao()

    suspend fun isDatabaseEmpty() = databaseDao.isDatabaseEmpty()

    suspend fun clearAllData() = db.clearAllData()

}