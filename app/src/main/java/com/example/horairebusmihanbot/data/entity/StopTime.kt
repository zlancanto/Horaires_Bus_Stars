package com.example.horairebusmihanbot.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
class StopTime {
    @Entity(
        tableName = "stop_time",
        foreignKeys = [
            ForeignKey(
                entity = Trip::class,
                parentColumns = ["trip_id"],
                childColumns = ["trip_id"],
                onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                entity = Stop::class,
                parentColumns = ["stop_id"],
                childColumns = ["stop_id"],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [
            Index("trip_id"),
            Index("stop_id")
        ]
    )
    data class StopTime(
        @PrimaryKey(autoGenerate = true)
        val uid: Int = 0,

        @ColumnInfo(name = "trip_id")
        val tripId: String,

        @ColumnInfo(name = "arrival_time")
        val arrivalTime: String?,

        @ColumnInfo(name = "departure_time")
        val departureTime: String?,

        @ColumnInfo(name = "stop_id")
        val stopId: String,

        @ColumnInfo(name = "stop_sequence")
        val stopSequence: Int
    )

}