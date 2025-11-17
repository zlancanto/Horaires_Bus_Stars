package com.example.horairebusmihanbot.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.horairebusmihanbot.data.entity.*
import com.example.horairebusmihanbot.data.dao.*

class AppDatabase {
    @Database(
        entities = [
            BusRoute::class,
            Trip::class,
            Stop::class,
            StopTime::class,
            Calendar::class
        ],
        version = 1,
        exportSchema = false
    )
    abstract class AppDatabase : RoomDatabase() {

        // DAO exposés en interne uniquement, car on utilise le design pattern repository
        abstract fun busRouteDao(): BusRouteDao
        abstract fun tripDao(): TripDao
        abstract fun stopDao(): StopDao
        abstract fun stopTimeDao(): StopTimeDao
        abstract fun calendarDao(): CalendarDao

        companion object {
            @Volatile
            private var INSTANCE: AppDatabase? = null

            fun getInstance(context: Context): AppDatabase {
                return INSTANCE ?: synchronized(this) {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "transport.db"
                    ).build().also { INSTANCE = it }
                }
            }
        }
    }

}