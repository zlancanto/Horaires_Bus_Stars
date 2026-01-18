package com.example.horairebusmihanbot.data.dto

data class StopTimeWithLabelDto (
    val departureTime: String,
    val stopId: String,
    val stopName: String,
    val stopSequence: Int
)