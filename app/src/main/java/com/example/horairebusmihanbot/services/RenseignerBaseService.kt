package com.example.horairebusmihanbot.services

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.data.entity.*
import com.example.horairebusmihanbot.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
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

    // Le dossier GTFS extraits via TelechargerFichiersService
    private val gtfsFolder = File(context.filesDir, "gtfs")

    // Les fichiers que l’on doit importer, dans l'ordre
    private val gtfsFiles = listOf(
        "routes.txt",
        "calendar.txt",
        "stops.txt",
        "trips.txt",
        "stop_times.txt"
    )

    private var notifId = 1000

    private var totalLinesToProcess = 0
    private var currentLinesProcessed = 0


    suspend fun startImport(onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        Log.i("IMPORT", "Début de l'importation depuis dossier GTFS : ${gtfsFolder.absolutePath}")

        onProgress(0f)

        // Compte total des lignes
        totalLinesToProcess = countTotalLines()
        currentLinesProcessed = 0

        if (totalLinesToProcess == 0) {
            Log.w("IMPORT", "Aucune ligne détectée. Vérifie que le ZIP est extrait.")
            onProgress(1f)
            return@withContext
        }

        // Import dans le bon ordre
        importRoutes(onProgress)
        sendNotification(context, "Routes importées")

        importCalendar(onProgress)
        sendNotification(context, "Calendrier importé")

        importStops(onProgress)
        sendNotification(context, "Stops importés")

        importTrips(onProgress)
        sendNotification(context, "Trips importés")

        importStopTimes(onProgress)
        sendNotification(context, "Stop times importés")

        onProgress(1f)

        val duration = (System.currentTimeMillis() - start) / 1000
        Log.i("IMPORT", "Import terminé en $duration secondes.")
    }


    private fun getGTFSFileStream(fileName: String): InputStream? {
        val file = File(gtfsFolder, fileName)

        return if (file.exists()) {
            FileInputStream(file)
        } else {
            Log.e("IMPORT", "Fichier GTFS introuvable : ${file.absolutePath}")
            null
        }
    }


    // Comptage des lignes pour la progression
    private fun countTotalLines(): Int {
        var count = 0

        gtfsFiles.forEach { fileName ->
            val file = File(gtfsFolder, fileName)

            if (!file.exists()) {
                Log.e("IMPORT", "Impossible de compter lignes (manque): $fileName")
                return@forEach
            }

            try {
                BufferedReader(InputStreamReader(FileInputStream(file))).useLines { seq ->
                    val lines = seq.count()
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
            getGTFSFileStream(fileName)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).useLines { lines ->
                    lines.drop(1)
                        .map { it.split(",") }
                        .mapNotNull { c ->
                            try {
                                mapper(c)
                            } catch (e: Exception) {
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
        parseFile(
            "routes.txt",
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
        parseFile(
            "calendar.txt",
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
        parseFile(
            "stops.txt",
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
        parseFile(
            "trips.txt",
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
        parseFile(
            "stop_times.txt",
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


    suspend fun clearDatabase() = withContext(Dispatchers.IO) {
        routeRepo.deleteAll()
        calendarRepo.deleteAll()
        stopRepo.deleteAll()
        tripRepo.deleteAll()
        stopTimeRepo.deleteAll()
        Log.i("DB", "Base de données vidée.")
    }


    private fun sendNotification(context: Context, contenu: String) {
        val hasPermission = ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.e("IMPORT", "Permission notification manquante")
            return
        }

        val nm = NotificationManagerCompat.from(context)
        val builder = NotificationCompat.Builder(context, "IMPORT_CHANNEL")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(contenu)
            .setContentText("Données copiées dans la BDD.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        try {
            nm.notify(notifId++, builder.build())
        } catch (e: Exception) {
            Log.e("IMPORT", "Erreur lors de la notification", e)
        }
    }
}
