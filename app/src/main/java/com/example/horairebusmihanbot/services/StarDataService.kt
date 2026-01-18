package com.example.horairebusmihanbot.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.horairebusmihanbot.MainApp
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.repository.SyncRepository
import com.example.horairebusmihanbot.state.SyncInfo
import com.example.horairebusmihanbot.state.SyncState
import com.example.horairebusmihanbot.utils.insertChunk
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream


class StarDataService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val database = MainApp.database
    private val notifId = 1
    private val channelId = "star_sync_channel"
    private val apiJsonUrl = "https://data.explore.star.fr/api/explore/v2.1/catalog/datasets/tco-busmetro-horaires-gtfs-versions-td/exports/json?lang=fr&timezone=Europe%2FBerlin"
    private val filesToProcess = listOf("routes.txt", "stops.txt", "trips.txt", "calendar.txt", "stop_times.txt")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Pour Android 14 (API 34) et plus
            startForeground(
                notifId,
                buildNotification(getString(R.string.fsync_notif_preparing_the_download)),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            // Pour les versions plus anciennes
            startForeground(notifId, buildNotification(getString(R.string.fsync_notif_preparing_the_download)))
        }

        serviceScope.launch {
            try {
                SyncRepository.update(SyncState.Progress(0, getString(R.string.fsync_notif_url_retrieval)))

                // ÉTAPE 1 : Récupérer l'URL du ZIP depuis le JSON
                val jsonResponse = downloadString()
                val zipUrl = extraireUrlDepuisJson(jsonResponse)

                // ÉTAPE 2 : Télécharger et extraire le ZIP en flux (Streaming)
                SyncRepository.update(SyncState.Progress(10, getString(R.string.fsync_notif_download_GTFS)))

                val request = Request.Builder().url(zipUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException(getString(R.string.fsync_toast_error_ZIP_download))

                    val inputStream = response.body?.byteStream()
                        ?: throw IOException(getString(R.string.fsync_toast_error_file_empty))

                    // On passe le flux directement au parseur (Optimal !)
                    processZipStream(inputStream)
                }

                SyncInfo.lastSyncTimestamp = android.icu.util.Calendar.getInstance()
                SyncRepository.update(SyncState.Finished)

            } catch (e: Exception) {
                Log.e("SYNC_ERROR", "Échec : ${e.message}")
                SyncRepository.update(SyncState.Error(e.message ?: getString(R.string.fsync_toast_error_unknown_error)))
            } finally {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun downloadString(): String {
        val request = Request.Builder().url(apiJsonUrl).build()
        return client.newCall(request).execute().use {
            if (!it.isSuccessful) throw IOException(getString(R.string.fsync_toast_error_JSON))
            it.body.string()
        }
    }

    private fun extraireUrlDepuisJson(json: String): String {
        // Ta logique Jackson qui fonctionnait bien
        val node = mapper.readTree(json)
        return node.firstOrNull()?.get("url")?.asText()
            ?: throw IOException(getString(R.string.fsync_toast_error_url_not_found_in_json))
    }

    private suspend fun processZipStream(inputStream: InputStream) {
        val zip = ZipInputStream(inputStream)
        var filesFound = 0

        // 1. Notification : Début du remplissage
        sendSimpleNotification( getString(R.string.fsync_notif_start_of_filling_title), getString(R.string.fsync_notif_start_of_filling_content))

        // Nettoyage initial obligatoire
        SyncRepository.update(SyncState.Progress(15, getString(R.string.fsync_notif_db_cleanup)))
        database.clearAllTables()

        zip.use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val fileName = entry.name
                if (fileName in filesToProcess) {
                    filesFound++
                    val progress = 20 + (filesFound * 75 / filesToProcess.size)

                    SyncRepository.update(SyncState.Progress(progress, "Traitement de $fileName..."))

                    // Appel de la fonction de lecture ligne par ligne (déjà vue ensemble)
                    readAndInsertFile(zis, fileName, progress)

                    sendSimpleNotification(
                        getString(R.string.fsync_notif_file_dowloaded_succesfully_title),
                        fileName + " " + getString(R.string.fsync_notif_file_dowloaded_succesfully_content)
                    )
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        if (filesFound == 0) {
            Log.e("SYNC_ERROR", "Aucun fichier GTFS valide trouvé dans le ZIP")
            throw IOException(getString(R.string.fsync_toast_error_gtfs_file_not_found))
        }

        SyncRepository.update(SyncState.Progress(100, "Synchronisation terminée !"))
        sendSimpleNotification(getString(R.string.fsync_notif_sync_complete_title), getString(R.string.fsync_notif_sync_complete_content))
    }

    private suspend fun readAndInsertFile(
        zip: ZipInputStream,
        fileName: String,
        basePercent: Int
    ) {

        val reader = zip.bufferedReader() // Extension plus propre
        reader.readLine() ?: return

        // On utilise une séquence pour traiter ligne par ligne sans saturer la RAM
        reader.lineSequence()
            .filter { it.isNotBlank() }
            .chunked(1000)
            .forEach { chunk ->
                insertChunk(fileName, chunk)
                // Mise à jour de l'état global
                SyncRepository.update(SyncState.Progress(basePercent, getString(R.string.fsync_importing_msg) + " $fileName..."))
            }
    }

    private fun sendSimpleNotification(title: String, text: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Ton icône
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt() and 0xfffffff, notification)
    }

    private fun buildNotification(text: String) = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle("STAR Data Sync")
        .setContentText(text)
        .setOngoing(true)
        .setProgress(100, 0, true)
        .build()

    private fun createNotificationChannel() {
        val channel = NotificationChannel(channelId, "Sync Data", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}