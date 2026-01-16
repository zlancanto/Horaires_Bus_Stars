package com.example.horairebusmihanbot

import android.app.Application
import androidx.room.Room
import com.example.horairebusmihanbot.data.AppDatabase
import com.example.horairebusmihanbot.repository.StarRepository

class MainApp : Application() {
    companion object {
        lateinit var database: AppDatabase
        lateinit var repository: StarRepository
    }
    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "star.db"
        ).build()

        repository = StarRepository(database.starDao())
    }
}