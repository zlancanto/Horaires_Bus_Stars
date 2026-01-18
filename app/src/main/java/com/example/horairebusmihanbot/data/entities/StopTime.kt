package com.example.horairebusmihanbot.data.entities

import androidx.room.Entity

@Entity(
    tableName = "stop_time",
    primaryKeys = ["tripId", "stopSequence"]
)
data class StopTime(
    val tripId: String,
    val arrivalTime: String,
    val departureTime: String,
    val stopId: String,
    val stopSequence: Int
)