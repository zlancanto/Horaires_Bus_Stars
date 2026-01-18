package com.example.horairebusmihanbot.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip",
    foreignKeys = [
        ForeignKey(
            entity = BusRoute::class,
            parentColumns = ["routeId"],
            childColumns = ["routeId"]
        )
    ]
)
data class Trip(
    @PrimaryKey
    val tripId: String,
    val routeId: String,
    val serviceId: String,
    val tripHeadsign: String,
    val directionId: Int
)