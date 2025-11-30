package com.example.horairebusmihanbot.services

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.horairebusmihanbot.R
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.zip.ZipInputStream
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelechargerFichiersService(private val context: Context) {

    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val apiJsonUrl =
        "https://data.explore.star.fr/api/explore/v2.1/catalog/datasets/tco-busmetro-horaires-gtfs-versions-td/exports/json?lang=fr&timezone=Europe%2FBerlin"

    // Fichiers GTFS que l’on veut extraire
    private val fichiersCibles = setOf(
        "calendar.txt",
        "routes.txt",
        "stop_times.txt",
        "stops.txt",
        "trips.txt"
    )

    suspend fun telechargerEtExtraire() = withContext(Dispatchers.IO) {

        try {
            // 1) Télécharger JSON
            val json = telecharger(apiJsonUrl)
            Log.e("DOWNLOAD", "Json downloaded")
            // 2) Trouver l’URL du ZIP
            val urlZip = extraireUrlDepuisJson(json)
            Log.e("DOWNLOAD", "Url zip extrait")

            // 3) Télécharger ZIP
            val zipBytes = telechargerBytes(urlZip)
            Log.e("DOWNLOAD", "Zip downloaded")
            sendNotification(context, "Fichier zip téléchargé")

            // 4) Extraire les fichiers dans le stockage interne
            extraireFichiers(zipBytes)
            Log.e("DOWNLOAD", "Fichiers extraits")

            // Succès
            Result.success(Unit)
        }
        catch (e: Exception) {
            Log.e("DOWNLOAD_FAIL", "Échec du processus de téléchargement et d'extraction.", e)
            Result.failure(e)
        }
    }


     private fun telecharger(url: String): String {
        val req = Request.Builder().url(url).build()
        try {
            client.newCall(req).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Erreur HTTP: ${response.code}")
                return response.body?.string() ?: throw Exception("Réponse vide")
            }
        }
        catch (e: Exception) {
            Log.e("NETWORK_ERROR", "Erreur lors du téléchargement de $url", e)
            throw e
        }
    }

    private fun telechargerBytes(url: String): ByteArray {
        val req = Request.Builder().url(url).build()
        try {
            client.newCall(req).execute().use {
                if (!it.isSuccessful) throw Exception("Erreur HTTP: ${it.code}")
                return it.body?.bytes() ?: throw Exception("Impossible de lire le ZIP")
            }
        }
        catch (e: Exception) {
            Log.e("NETWORK_ERROR", "Erreur lors du téléchargement de $url", e)
            throw e
        }
    }

    private fun extraireUrlDepuisJson(json: String): String {
        // Le JSON renvoie une *liste* d'objets
        val liste: List<Map<String, Any?>> = mapper.readValue(json)

        val premier = liste.firstOrNull()
            ?: throw Exception("JSON vide")

        return premier["url"] as? String
            ?: throw Exception("Clé 'url' introuvable dans JSON")
    }

    private fun extraireFichiers(zipData: ByteArray) {
        val dossierGTFS = File(context.filesDir, "gtfs")

        if (!dossierGTFS.exists()) {
            dossierGTFS.mkdirs()
        }

        ZipInputStream(zipData.inputStream()).use { zip ->
            var entry = zip.nextEntry

            while (entry != null) {
                val fileName = entry.name

                if (fileName in fichiersCibles) {
                    println("Extraction du fichier : $fileName")

                    val outFile = File(dossierGTFS, fileName)

                    outFile.outputStream().use { output ->
                        zip.copyTo(output)
                    }
                }

                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
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
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.Default) {
            try {
                val nm = NotificationManagerCompat.from(context)
                val builder = NotificationCompat.Builder(context, "IMPORT_CHANNEL")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(contenu)
                    .setContentText("Données copiées dans la BDD.")
                    .setPriority(NotificationCompat.PRIORITY_LOW) // moins bruyant

                // Utiliser un notifId unique à chaque fois
                nm.notify(System.currentTimeMillis().toInt() and 0xfffffff, builder.build())
            } catch (e: Exception) {
                Log.e("IMPORT", "Erreur lors de la notification", e)
            }
        }
    }

}
