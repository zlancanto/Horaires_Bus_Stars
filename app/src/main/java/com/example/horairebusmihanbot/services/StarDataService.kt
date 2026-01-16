package com.example.horairebusmihanbot.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.horairebusmihanbot.MainApp
import com.example.horairebusmihanbot.model.*
import com.example.horairebusmihanbot.repository.SyncRepository
import com.example.horairebusmihanbot.repository.SyncState
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class StarDataService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val NOTIF_ID = 1
    private val CHANNEL_ID = "star_sync_channel"
    val API_JSON_URL = "https://data.explore.star.fr/api/explore/v2.1/catalog/datasets/tco-busmetro-horaires-gtfs-versions-td/exports/json?lang=fr&timezone=Europe%2FBerlin"
    val FILES_TO_PROCESS = listOf("routes.txt", "stops.txt", "trips.txt", "calendar.txt", "stop_times.txt")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Pour Android 14 (API 34) et plus
            startForeground(
                NOTIF_ID,
                buildNotification("Préparation du téléchargement...", 0),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            // Pour les versions plus anciennes
            startForeground(NOTIF_ID, buildNotification("Préparation du téléchargement...", 0))
        }

        serviceScope.launch {
            try {
                SyncRepository.update(SyncState.Progress(0, "Récupération de l'URL..."))

                // ÉTAPE 1 : Récupérer l'URL du ZIP depuis le JSON
                val jsonResponse = downloadString(API_JSON_URL)
                val zipUrl = extraireUrlDepuisJson(jsonResponse)

                // ÉTAPE 2 : Télécharger et extraire le ZIP en flux (Streaming)
                SyncRepository.update(SyncState.Progress(10, "Téléchargement du GTFS..."))

                val request = Request.Builder().url(zipUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Erreur téléchargement ZIP")

                    val inputStream = response.body?.byteStream()
                        ?: throw IOException("Fichier vide")

                    // On passe le flux directement au parseur (Optimal !)
                    processZipStream(inputStream)
                }

                SyncRepository.update(SyncState.Finished)

            } catch (e: Exception) {
                Log.e("SYNC_ERROR", "Échec : ${e.message}")
                SyncRepository.update(SyncState.Error(e.message ?: "Erreur inconnue"))
            } finally {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun downloadString(url: String): String {
        val request = Request.Builder().url(url).build()
        return client.newCall(request).execute().use {
            if (!it.isSuccessful) throw IOException("Erreur JSON")
            it.body?.string() ?: ""
        }
    }

    private fun extraireUrlDepuisJson(json: String): String {
        // Ta logique Jackson qui fonctionnait bien
        val node = mapper.readTree(json)
        return node.firstOrNull()?.get("url")?.asText()
            ?: throw IOException("URL non trouvée dans le JSON")
    }

    private suspend fun processZipStream(inputStream: InputStream) {
        val dao = MainApp.database.starDao()
        val zip = ZipInputStream(inputStream)

        var filesFound = 0

        // Nettoyage initial obligatoire
        dao.clearAllTables()

        zip.use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name in FILES_TO_PROCESS) {
                    filesFound++
                    val basePercent = (filesFound * 100) / FILES_TO_PROCESS.size

                    // Lecture optimisée ligne par ligne
                    readAndInsertFile(zis, entry.name, dao, basePercent)
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        if (filesFound == 0) {
            Log.e("SYNC_ERROR", "Aucun fichier GTFS valide trouvé dans le ZIP")
            throw IOException("Aucun fichier GTFS valide trouvé dans le ZIP")
        }
    }

    private suspend fun readAndInsertFile(
        zip: ZipInputStream,
        fileName: String,
        dao: StarDao,
        basePercent: Int
    ) { // On force l'exécution sur le pool IO

        val reader = zip.bufferedReader() // Extension plus propre
        val header = reader.readLine() ?: return

        // On utilise une séquence pour traiter ligne par ligne sans saturer la RAM
        reader.lineSequence()
            .filter { it.isNotBlank() }
            .chunked(1000)
            .forEach { chunk ->
                insertChunk(fileName, chunk, dao)
                // Mise à jour de l'état global
                SyncRepository.update(SyncState.Progress(basePercent, "Importation de $fileName..."))
            }
    }

    private suspend fun insertChunk(fileName: String, lines: List<String>, dao: StarDao) {
        // Pattern Strategy : On délègue le parsing selon le fichier
        when (fileName) {
            "routes.txt" -> dao.insertRoutes(lines.map { parseRoute(it) })
            "stops.txt" -> dao.insertStops(lines.map { parseStop(it) })
            "trips.txt" -> dao.insertTrips(lines.map { parseTrip(it) })
            "calendar.txt" -> dao.insertCalendars(lines.map { parseCalendar(it) })
            "stop_times.txt" -> dao.insertStopTimes(lines.map { parseStopTime(it) })
        }
    }

    // --- LOGIQUE DE PARSING (Clean Code : Une fonction par responsabilité) ---

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

    // --- NOTIFICATIONS ---

    private fun sendTableFinishedNotification(tableName: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Table synchronisée")
            .setContentText("Le fichier $tableName a été importé avec succès.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(tableName.hashCode(), notification)
    }

    private fun updateMainNotification(text: String, progress: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, buildNotification(text, progress))
    }

    private fun buildNotification(text: String, progress: Int) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle("STAR Data Sync")
        .setContentText(text)
        .setOngoing(true)
        .setProgress(100, progress, progress == 0)
        .build()

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Sync Data", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}