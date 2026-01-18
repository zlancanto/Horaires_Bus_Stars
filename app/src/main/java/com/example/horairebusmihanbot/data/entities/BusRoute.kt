package com.example.horairebusmihanbot.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bus_route")
data class BusRoute(
    @PrimaryKey
    val routeId: String,
    val routeShortName: String,
    val routeLongName: String,
    val routeColor: String,
    val routeTextColor: String
)