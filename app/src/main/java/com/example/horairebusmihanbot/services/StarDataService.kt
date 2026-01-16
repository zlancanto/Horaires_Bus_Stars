package com.example.horairebusmihanbot.services

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.horairebusmihanbot.MainApp
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.model.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.util.zip.ZipInputStream

class StarDataService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val NOTIF_ID = 101
    private val CHANNEL_ID = "star_sync"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Démarrage...", 0))

        scope.launch {
            try {
                val url = "https://data.explore.star.fr/explore/dataset/tco-busmetro-horaires-gtfs-versions-td/files/de3bd18de46a67895d33bbd1c8109316/download/"
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()

                updateNotif("Téléchargement du ZIP...", 10)
                val response = client.newCall(request).execute()
                val inputStream = response.body?.byteStream() ?: return@launch

                parseZip(inputStream)

                updateNotif("Base de données prête", 100)
                stopForeground(STOP_FOREGROUND_DETACH)
            } catch (e: Exception) {
                updateNotif("Erreur : ${e.message}", 0)
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun parseZip(inputStream: InputStream) {
        val dao = MainApp.database.starDao()
        dao.deleteAll()

        val zip = ZipInputStream(inputStream)
        var entry = zip.nextEntry
        while (entry != null) {
            val reader = BufferedReader(InputStreamReader(zip))
            val lines = reader.readLines().drop(1) // Skip header

            when (entry.name) {
                "routes.txt" -> {
                    dao.insertRoutes(lines.map { it.split(",").let { t -> BusRoute(t[0], t[2], t[3], t[7], t[8]) } })
                    sendTableNotification("Routes")
                }
                "stops.txt" -> {
                    dao.insertStops(lines.map { it.split(",").let { t -> Stop(t[0], t[2], t[4].toDouble(), t[5].toDouble()) } })
                    sendTableNotification("Stops")
                }
                "trips.txt" -> {
                    dao.insertTrips(lines.map { it.split(",").let { t -> Trip(t[2], t[0], t[1], t[3], t[5].toInt()) } })
                    sendTableNotification("Trips")
                }
                "calendar.txt" -> {
                    dao.insertCalendars(lines.map { it.split(",").let { t -> Calendar(t[0], t[1].toInt(), t[2].toInt(), t[3].toInt(), t[4].toInt(), t[5].toInt(), t[6].toInt(), t[7].toInt(), t[8], t[9]) } })
                    sendTableNotification("Calendar")
                }
                "stop_times.txt" -> {
                    lines.chunked(1000).forEach { chunk ->
                        dao.insertStopTimes(chunk.map { it.split(",").let { t -> StopTime(t[0], t[1], t[2], t[3], t[4].toInt()) } })
                    }
                    sendTableNotification("StopTimes")
                }
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
    }

    private fun sendTableNotification(tableName: String) {
        val n = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Table terminée")
            .setContentText("Le remplissage de $tableName est fini.")
            .build()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(tableName.hashCode(), n)
    }

    private fun updateNotif(text: String, progress: Int) {
        val n = buildNotification(text, progress)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIF_ID, n)
    }

    private fun buildNotification(text: String, progress: Int) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle("Synchronisation STAR")
        .setContentText(text)
        .setProgress(100, progress, false)
        .build()

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Sync", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}