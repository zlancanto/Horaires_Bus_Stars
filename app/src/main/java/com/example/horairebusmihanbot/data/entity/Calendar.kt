package com.example.horairebusmihanbot.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

class Calendar {
    @Entity(
        tableName = "calendar"
    )
    data class Calendar(
        @PrimaryKey
        @ColumnInfo(name = "service_id")
        val serviceId: String,

        @ColumnInfo(name = "monday")
        val monday: Int,

        @ColumnInfo(name = "tuesday")
        val tuesday: Int,

        @ColumnInfo(name = "wednesday")
        val wednesday: Int,

        @ColumnInfo(name = "thursday")
        val thursday: Int,

        @ColumnInfo(name = "friday")
        val friday: Int,

        @ColumnInfo(name = "saturday")
        val saturday: Int,

        @ColumnInfo(name = "sunday")
        val sunday: Int,

        @ColumnInfo(name = "start_date")
        val startDate: String,

        @ColumnInfo(name = "end_date")
        val endDate: String
    )

}