package com.example.horairebusmihanbot.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
class Trip {
    @Entity(
        tableName = "trip",
        foreignKeys = [
            ForeignKey(
                entity = BusRoute::class,
                parentColumns = ["route_id"],
                childColumns = ["route_id"],
                onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                entity = Calendar::class,
                parentColumns = ["service_id"],
                childColumns = ["service_id"],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [
            Index("route_id"),
            Index("service_id")
        ]
    )
    data class Trip(
        @PrimaryKey
        @ColumnInfo(name = "trip_id")
        val id: String,

        @ColumnInfo(name = "route_id")
        val routeId: String,

        @ColumnInfo(name = "service_id")
        val serviceId: String,

        @ColumnInfo(name = "trip_headsign")
        val headsign: String?,

        @ColumnInfo(name = "direction_id")
        val directionId: Int?,

        @ColumnInfo(name = "block_id")
        val blockId: String?,

        @ColumnInfo(name = "wheelchair_accessible")
        val wheelchairAccessible: Int?
    )

}