package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entities.StopTime

@Dao
interface StopTimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopTimes(times: List<StopTime>)

    @Query(
        """
        SELECT st.* FROM stop_time st
        INNER JOIN trip t ON st.tripId = t.tripId
        INNER JOIN calendar c ON t.serviceId = c.serviceId
        WHERE st.stopId = :stopId 
        AND t.routeId = :routeId 
        AND t.directionId = :directionId
        AND st.departureTime >= :currentTime
        AND (
            (:day = 'monday' AND c.monday = 1) OR
            (:day = 'tuesday' AND c.tuesday = 1) OR
            (:day = 'wednesday' AND c.wednesday = 1) OR
            (:day = 'thursday' AND c.thursday = 1) OR
            (:day = 'friday' AND c.friday = 1) OR
            (:day = 'saturday' AND c.saturday = 1) OR
            (:day = 'sunday' AND c.sunday = 1)
        )
        ORDER BY st.departureTime ASC
    """
    )
    suspend fun getNextPassages(
        stopId: String,
        routeId: String,
        directionId: Int,
        currentTime: String, // Format "HH:mm:ss"
        day: String          // ex: "monday"
    ): List<StopTime>

    @Query("DELETE FROM stop_time")
    suspend fun deleteAllStopTimes()
}