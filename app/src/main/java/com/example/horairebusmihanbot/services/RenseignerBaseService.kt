package com.example.horairebusmihanbot.services

import android.content.Context
import android.util.Log
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.data.entity.*
import com.example.horairebusmihanbot.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class RenseignerBaseService(
    private val context: Context,
    private val routeRepo: BusRouteRepository,
    private val calendarRepo: CalendarRepository,
    private val stopRepo: StopRepository,
    private val tripRepo: TripRepository,
    private val stopTimeRepo: StopTimeRepository
) {

    private val rawFiles: Map<String, Int> = mapOf(
        "routes.txt" to R.raw.routes,
        "calendar.txt" to R.raw.calendar,
        "stops.txt" to R.raw.stops,
        "trips.txt" to R.raw.trips,
        "stop_times.txt" to R.raw.stop_times
    )

    private var totalLinesToProcess = 0
    private var currentLinesProcessed = 0

    suspend fun startImport(onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        Log.i("IMPORT", "Début de l'importation...")

        onProgress(0f)
        totalLinesToProcess = countTotalLines()
        currentLinesProcessed = 0

        if (totalLinesToProcess == 0) {
            Log.w("IMPORT", "Aucune ligne à traiter.")
            onProgress(1f)
            return@withContext
        }

        importRoutes(onProgress)
        importCalendar(onProgress)
        importStops(onProgress)
        importTrips(onProgress)
        importStopTimes(onProgress)

        onProgress(1f)
        val duration = (System.currentTimeMillis() - start) / 1000
        Log.i("IMPORT", "Import terminé en $duration secondes.")
    }

    private fun getResourceStream(fileName: String): InputStream? {
        val rawId = rawFiles[fileName]
        if (rawId == null) {
            Log.e("IMPORT", "Fichier $fileName introuvable.")
            return null
        }
        return context.resources.openRawResource(rawId)
    }

    private fun countTotalLines(): Int {
        var count = 0

        rawFiles.forEach { (fileName, rawId) ->
            try {
                context.resources.openRawResource(rawId).use { stream ->
                    val lines = BufferedReader(InputStreamReader(stream))
                        .useLines { it.count() }

                    count += (lines - 1).coerceAtLeast(0)
                }
            } catch (e: Exception) {
                Log.e("IMPORT", "Erreur comptage $fileName : ${e.message}")
            }
        }

        return count
    }

    private suspend fun <T> parseFile(
        fileName: String,
        mapper: (List<String>) -> T?,
        inserter: suspend (List<T>) -> Unit,
        onProgress: (Float) -> Unit
    ) {
        try {
            getResourceStream(fileName)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).useLines { lines ->
                    lines.drop(1)
                        .map { it.split(",") }
                        .mapNotNull { c ->
                            try { mapper(c) }
                            catch (e: Exception) {
                                Log.w("IMPORT", "Erreur mapping $fileName : ${e.message}")
                                null
                            }
                        }
                        .chunked(20000)
                        .forEach { batch ->
                            inserter(batch)
                            currentLinesProcessed += batch.size
                            onProgress(currentLinesProcessed.toFloat() / totalLinesToProcess)
                        }
                }
            } ?: Log.e("IMPORT", "Fichier introuvable : $fileName")
        } catch (e: Exception) {
            Log.e("IMPORT", "Erreur critique dans $fileName : ${e.message}")
        }
    }

    private suspend fun importRoutes(onProgress: (Float) -> Unit) {
        parseFile("routes.txt",
            mapper = { c ->
                BusRoute(
                    id = c[0],
                    shortName = c.getOrNull(2),
                    longName = c.getOrNull(3),
                    description = c.getOrNull(4),
                    type = c.getOrNull(5)?.toIntOrNull(),
                    color = c.getOrNull(7),
                    textColor = c.getOrNull(8)
                )
            },
            inserter = { routeRepo.insertAll(it) },
            onProgress = onProgress
        )
    }

    private suspend fun importCalendar(onProgress: (Float) -> Unit) {
        parseFile("calendar.txt",
            mapper = { c ->
                Calendar(
                    serviceId = c[0],
                    monday = c[1].toIntOrNull() ?: 0,
                    tuesday = c[2].toIntOrNull() ?: 0,
                    wednesday = c[3].toIntOrNull() ?: 0,
                    thursday = c[4].toIntOrNull() ?: 0,
                    friday = c[5].toIntOrNull() ?: 0,
                    saturday = c[6].toIntOrNull() ?: 0,
                    sunday = c[7].toIntOrNull() ?: 0,
                    startDate = c[8],
                    endDate = c[9]
                )
            },
            inserter = { calendarRepo.insertAll(it) },
            onProgress = onProgress
        )
    }

    private suspend fun importStops(onProgress: (Float) -> Unit) {
        parseFile("stops.txt",
            mapper = { c ->
                Stop(
                    id = c[0],
                    name = c[2],
                    description = c.getOrNull(3)?.ifEmpty { null },
                    latitude = c[4].toDoubleOrNull() ?: 0.0,
                    longitude = c[5].toDoubleOrNull() ?: 0.0,
                    wheelchairBoarding = c.getOrNull(10)?.toIntOrNull()
                )
            },
            inserter = { stopRepo.insertAll(it) },
            onProgress = onProgress
        )
    }

    private suspend fun importTrips(onProgress: (Float) -> Unit) {
        parseFile("trips.txt",
            mapper = { c ->
                Trip(
                    routeId = c[0],
                    serviceId = c[1],
                    id = c[2],
                    headsign = c.getOrNull(3),
                    directionId = c.getOrNull(5)?.toIntOrNull(),
                    blockId = c.getOrNull(6),
                    wheelchairAccessible = c.getOrNull(8)?.toIntOrNull()
                )
            },
            inserter = { tripRepo.insertAll(it) },
            onProgress = onProgress
        )
    }

    private suspend fun importStopTimes(onProgress: (Float) -> Unit) {
        parseFile("stop_times.txt",
            mapper = { c ->
                StopTime(
                    tripId = c[0],
                    arrivalTime = c[1],
                    departureTime = c[2],
                    stopId = c[3],
                    stopSequence = c[4].toIntOrNull() ?: 0
                )
            },
            inserter = { stopTimeRepo.insertAll(it) },
            onProgress = onProgress
        )
    }

    private fun List<String>.getOrNull(index: Int): String? =
        if (index < size) this[index] else null
}
