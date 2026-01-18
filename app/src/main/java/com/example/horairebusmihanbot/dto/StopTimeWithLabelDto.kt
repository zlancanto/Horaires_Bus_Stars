package com.example.horairebusmihanbot.dto

data class StopTimeWithLabelDto (
    val departure_time: String,
    val stop_id: String,
    val stop_name: String,
    val stop_sequence: Int
)