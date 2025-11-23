package com.example.horairebusmihanbot.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.horairebusmihanbot.data.dao.*
import com.example.horairebusmihanbot.data.entity.*

@Database(
    entities = [
        BusRoute::class,
        Calendar::class,
        Stop::class,
        Trip::class,
        StopTime::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAO exposés pour que les Repositories puissent les utiliser
    abstract fun busRouteDao(): BusRouteDao
    abstract fun calendarDao(): CalendarDao
    abstract fun stopDao(): StopDao
    abstract fun tripDao(): TripDao
    abstract fun stopTimeDao(): StopTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "transport.db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}