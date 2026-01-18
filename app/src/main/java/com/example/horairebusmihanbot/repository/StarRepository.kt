package com.example.horairebusmihanbot.repository

import com.example.horairebusmihanbot.data.AppDatabase

class StarRepository(private val db: AppDatabase) {
    val routes = BusRouteRepository(db)
    val stops = StopRepository(db)
    val trips = TripRepository(db)
    val directions = DirectionRepository(db)
    val stopTimes = StopTimeRepository(db)
    val calendar = CalendarRepository(db)
    val database = DatabaseRepository(db)
}