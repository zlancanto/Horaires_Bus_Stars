package com.example.horairebusmihanbot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horairebusmihanbot.data.entities.Stop

@Dao
interface StopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<Stop>)

    @Query(
        """
        SELECT s.* FROM stop s 
        JOIN stop_time st ON s.stopId = st.stopId 
        JOIN trip t ON st.tripId = t.tripId 
        WHERE t.routeId = :routeId AND t.directionId = :directionId 
        GROUP BY s.stopId ORDER BY st.stopSequence
    """
    )
    suspend fun getStops(routeId: String, directionId: Int): List<Stop>

    @Query("DELETE FROM stop")
    suspend fun deleteAllStops()
}