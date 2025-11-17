package com.example.horairebusmihanbot.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

class BusRoute {
    @Entity(
        tableName = "bus_route"
    )
    data class BusRoute(
        @PrimaryKey
        @ColumnInfo(name = "route_id")
        val id: String,

        @ColumnInfo(name = "route_short_name")
        val shortName: String?,

        @ColumnInfo(name = "route_long_name")
        val longName: String?,

        @ColumnInfo(name = "route_desc")
        val description: String?,

        @ColumnInfo(name = "route_type")
        val type: Int?,

        @ColumnInfo(name = "route_color")
        val color: String?,

        @ColumnInfo(name = "route_text_color")
        val textColor: String?
    )

}