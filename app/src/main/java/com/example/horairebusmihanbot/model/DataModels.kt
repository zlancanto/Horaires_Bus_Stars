package com.example.horairebusmihanbot.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction

@Entity(tableName = "bus_route")
data class BusRoute(
    @PrimaryKey val route_id: String,
    val route_short_name: String,
    val route_long_name: String,
    val route_color: String,
    val route_text_color: String
)

@Entity(tableName = "calendar")
data class Calendar(
    @PrimaryKey val service_id: String,
    val monday: Int, val tuesday: Int, val wednesday: Int,
    val thursday: Int, val friday: Int, val saturday: Int, val sunday: Int,
    val start_date: String, val end_date: String
)

@Entity(tableName = "stop")
data class Stop(
    @PrimaryKey val stop_id: String,
    val stop_name: String,
    val stop_lat: Double,
    val stop_lon: Double
)

@Entity(tableName = "trip", foreignKeys = [
    ForeignKey(entity = BusRoute::class, parentColumns = ["route_id"], childColumns = ["route_id"])
])
data class Trip(
    @PrimaryKey val trip_id: String,
    val route_id: String,
    val service_id: String,
    val trip_headsign: String,
    val direction_id: Int
)

@Entity(tableName = "stop_time", primaryKeys = ["trip_id", "stop_sequence"])
data class StopTime(
    val trip_id: String,
    val arrival_time: String,
    val departure_time: String,
    val stop_id: String,
    val stop_sequence: Int
)

@Dao
interface StarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<BusRoute>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<Stop>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<Trip>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendars(calendars: List<Calendar>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopTimes(times: List<StopTime>)


    @Query("SELECT * FROM bus_route")
    fun getAllRoutes(): LiveData<List<BusRoute>>

    @Query("SELECT DISTINCT trip_headsign, direction_id FROM trip WHERE route_id = :routeId")
    suspend fun getDirections(routeId: String): List<DirectionInfo>

    @Query("""
        SELECT s.* FROM stop s 
        JOIN stop_time st ON s.stop_id = st.stop_id 
        JOIN trip t ON st.trip_id = t.trip_id 
        WHERE t.route_id = :rId AND t.direction_id = :dId 
        GROUP BY s.stop_id ORDER BY st.stop_sequence
    """)
    suspend fun getStops(rId: String, dId: Int): List<Stop>
    @Query("DELETE FROM bus_route")
    suspend fun deleteAllRoutes()

    @Query("DELETE FROM stop")
    suspend fun deleteAllStops()

    @Query("DELETE FROM trip")
    suspend fun deleteAllTrips()

    @Query("DELETE FROM calendar")
    suspend fun deleteAllCalendars()

    @Query("DELETE FROM stop_time")
    suspend fun deleteAllStopTimes()

    @Transaction
    suspend fun clearAllTables() {
        deleteAllStopTimes()
        deleteAllTrips()
        deleteAllCalendars()
        deleteAllStops()
        deleteAllRoutes()
    }

    @Query("SELECT (SELECT COUNT(*) FROM bus_route) == 0")
    suspend fun isDatabaseEmpty(): Boolean
}

data class DirectionInfo(val trip_headsign: String, val direction_id: Int)