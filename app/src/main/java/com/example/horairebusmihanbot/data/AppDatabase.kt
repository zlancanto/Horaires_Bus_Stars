package com.example.horairebusmihanbot.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.example.horairebusmihanbot.data.dao.BusRouteDao
import com.example.horairebusmihanbot.data.dao.CalendarDao
import com.example.horairebusmihanbot.data.dao.DatabaseDao
import com.example.horairebusmihanbot.data.dao.DirectionDao
import com.example.horairebusmihanbot.data.dao.StopDao
import com.example.horairebusmihanbot.data.dao.StopTimeDao
import com.example.horairebusmihanbot.data.dao.TripDao
import com.example.horairebusmihanbot.data.entities.BusRoute
import com.example.horairebusmihanbot.data.entities.Calendar
import com.example.horairebusmihanbot.data.entities.Stop
import com.example.horairebusmihanbot.data.entities.StopTime
import com.example.horairebusmihanbot.data.entities.Trip

/**
 * Patron de conception : Singleton (géré via MainApp)
 * Cette classe définit la configuration de la base de données Room.
 */
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
    abstract fun routeDao(): BusRouteDao
    abstract fun stopDao(): StopDao
    abstract fun tripDao(): TripDao
    abstract fun stopTimeDao(): StopTimeDao
    abstract fun calendarDao(): CalendarDao
    abstract fun directionDao(): DirectionDao
    abstract fun databaseDao(): DatabaseDao
    

    @Transaction
    suspend fun clearAllData() {
        routeDao().deleteAllRoutes()
        stopDao().deleteAllStops()
        tripDao().deleteAllTrips()
        stopTimeDao().deleteAllStopTimes()
        calendarDao().deleteAllCalendars()
    }
}