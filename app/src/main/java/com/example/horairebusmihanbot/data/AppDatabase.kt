package com.example.horairebusmihanbot.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.horairebusmihanbot.model.*

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

    // Cette fonction permet à l'application d'accéder aux commandes SQL
    abstract fun starDao(): StarDao
}