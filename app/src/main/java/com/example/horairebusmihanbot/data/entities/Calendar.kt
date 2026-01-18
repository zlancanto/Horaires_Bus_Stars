package com.example.horairebusmihanbot.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar")
data class Calendar(
    @PrimaryKey
    val serviceId: String,
    val monday: Int,
    val tuesday: Int,
    val wednesday: Int,
    val thursday: Int,
    val friday: Int,
    val saturday: Int,
    val sunday: Int,
    val startDate: String,
    val endDate: String
)