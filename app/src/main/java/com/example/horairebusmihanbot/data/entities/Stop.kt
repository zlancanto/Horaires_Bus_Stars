package com.example.horairebusmihanbot.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stop")
data class Stop(
    @PrimaryKey
    val stopId: String,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double
)