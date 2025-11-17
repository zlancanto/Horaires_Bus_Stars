package com.example.horairebusmihanbot.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

class Stop {
    @Entity(
        tableName = "stop"
    )
    data class Stop(
        @PrimaryKey
        @ColumnInfo(name = "stop_id")
        val id: String,

        @ColumnInfo(name = "stop_name")
        val name: String,

        @ColumnInfo(name = "stop_desc")
        val description: String?,

        @ColumnInfo(name = "stop_lat")
        val latitude: Double,

        @ColumnInfo(name = "stop_lon")
        val longitude: Double,

        @ColumnInfo(name = "wheelchair_boarding")
        val wheelchairBoarding: Int?
    )

}