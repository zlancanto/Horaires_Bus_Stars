package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.horairebusmihanbot.data.dto.DirectionDto

@Dao
interface DirectionDao {
    @Query("SELECT tripHeadsign, directionId FROM trip WHERE routeId = :routeId")
    suspend fun getDirections(routeId: String): List<DirectionDto>
}