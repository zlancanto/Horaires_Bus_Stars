package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entities.Trip
import com.example.horairebusmihanbot.data.dto.StopTimeWithLabelDto

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<Trip>)

    @Query(
        """
        SELECT st.departureTime, st.stopId, s.stopName, st.stopSequence 
        FROM stop_time st
        INNER JOIN stop s ON st.stopId = s.stopId
        WHERE st.tripId = :tripId
        AND st.stopSequence >= :stopSequence
        ORDER BY st.stopSequence ASC
    """
    )
    suspend fun getTripDetails(tripId: String, stopSequence: Int): List<StopTimeWithLabelDto>

    @Query("DELETE FROM trip")
    suspend fun deleteAllTrips()
}