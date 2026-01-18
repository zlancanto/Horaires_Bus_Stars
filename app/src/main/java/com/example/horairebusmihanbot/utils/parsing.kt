package com.example.horairebusmihanbot.utils

import com.example.horairebusmihanbot.MainApp.Companion.repository
import com.example.horairebusmihanbot.data.entities.BusRoute
import com.example.horairebusmihanbot.data.entities.Calendar
import com.example.horairebusmihanbot.data.entities.Stop
import com.example.horairebusmihanbot.data.entities.StopTime
import com.example.horairebusmihanbot.data.entities.Trip

suspend fun insertChunk(fileName: String, lines: List<String>) {
    // Pattern Strategy : On délègue le parsing selon le fichier
    when (fileName) {
        "routes.txt" -> repository.routes.insertRoutes(lines.map { parseRoute(it) })
        "stops.txt" -> repository.stops.insertStops(lines.map { parseStop(it) })
        "trips.txt" -> repository.trips.insertTrips(lines.map { parseTrip(it) })
        "calendar.txt" -> repository.calendar.insertCalendars(lines.map { parseCalendar(it) })
        "stop_times.txt" -> repository.stopTimes.insertStopTimes(lines.map { parseStopTime(it) })
    }
}

private fun parseRoute(line: String): BusRoute {
    val t = splitCsv(line)
    return BusRoute(t[0], t[2], t[3], t[7], t[8])
}

private fun parseStop(line: String): Stop {
    val t = splitCsv(line)
    return Stop(t[0], t[2], t[4].toDouble(), t[5].toDouble())
}

private fun parseTrip(line: String): Trip {
    val t = splitCsv(line)
    return Trip(t[2], t[0], t[1], t[3], t[5].toInt())
}

private fun parseCalendar(line: String): Calendar {
    val t = splitCsv(line)
    return Calendar(t[0], t[1].toInt(), t[2].toInt(), t[3].toInt(), t[4].toInt(),
        t[5].toInt(), t[6].toInt(), t[7].toInt(), t[8], t[9])
}

private fun parseStopTime(line: String): StopTime {
    val t = splitCsv(line)
    return StopTime(t[0], t[1], t[2], t[3], t[4].toInt())
}

private fun splitCsv(line: String): List<String> {
    // Regex qui gère les virgules entre guillemets
    return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
        .map { it.trim().removeSurrounding("\"") }
}