package com.example.horairebusmihanbot.services

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.exception.DataFormatException
import com.example.horairebusmihanbot.exception.ZipFileException
import com.fasterxml.jackson.core.JsonParseException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.zip.ZipInputStream
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.zip.ZipEntry

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
            val json = telecharger(apiJsonUrl)
            Log.e("DOWNLOAD", "Json downloaded")

            val urlZip = extraireUrlDepuisJson(json)
            Log.e("DOWNLOAD", "Url zip extrait")

            val zipBytes = telechargerBytes(urlZip)
            Log.e("DOWNLOAD", "Zip downloaded")
            sendNotification(context, "Fichier zip téléchargé")

            extraireFichiers(zipBytes)
            Log.e("DOWNLOAD", "Fichiers extraits")

            // Succès
            Result.success(Unit)
        }
        catch (e: SocketTimeoutException) {
            Log.e("DOWNLOAD_FAIL", "Échec de la connexion (Timeout).", e)
            sendNotification(context, "Erreur réseau : Le serveur a mis trop de temps à répondre.")
            Result.failure(e)
        }
        catch (e: ConnectException) {
            Log.e("DOWNLOAD_FAIL", "Échec de la connexion au serveur.", e)
            sendNotification(context, "Erreur réseau : Impossible d'établir la connexion au serveur.")
            Result.failure(e)
        }
        catch (e: IOException) {
            /*
             * Couvre les erreurs HTTP, les erreurs DNS,
             * et les erreurs d'écriture/lecture de fichiers
             */
            Log.e("DOWNLOAD_FAIL", "Erreur d'entrée/sortie lors du téléchargement ou de l'extraction.", e)
            sendNotification(context, "Erreur d'E/S : Problème réseau ou d'accès au stockage.")
            Result.failure(e)
        }
        catch (e: DataFormatException) {
            Log.e("DOWNLOAD_FAIL", "Échec du traitement des données JSON.", e)
            sendNotification(context, "Erreur de format : Les données JSON reçues sont invalides.")
            Result.failure(e)
        }
        catch (e: ZipFileException) {
            Log.e("DOWNLOAD_FAIL", "Échec de l'extraction du ZIP.", e)
            sendNotification(context, "Erreur d'archive : Le fichier ZIP est corrompu ou invalide.")
            Result.failure(e)
        }
        catch (e: Exception) {
            Log.e("DOWNLOAD_FAIL", "Échec général du processus.", e)
            sendNotification(context, "Erreur inconnue : Impossible de terminer l'opération.")
            Result.failure(e)
        }
    }

    private fun telecharger(url: String): String {
        val req = Request.Builder().url(url).build()
        try {
            client.newCall(req).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Erreur HTTP ${response.code} lors du téléchargement du JSON.")
                }
                return response.body?.string()
                    ?: throw DataFormatException("Le corps de la réponse JSON est vide.")
            }
        }
        // Interception des erreurs réseau spécifiques d'OkHttp
        catch (e: ConnectException) {
            throw ConnectException("Erreur de connexion au serveur pour l'URL $url.").initCause(e)
        } catch (e: SocketTimeoutException) {
            throw SocketTimeoutException("Délai de connexion expiré pour l'URL $url.").initCause(e)
        } catch (e: IOException) {
            throw IOException("Erreur d'entrée/sortie lors du téléchargement de $url.", e)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun telechargerBytes(url: String): ByteArray {
        val req = Request.Builder().url(url).build()
        try {
            client.newCall(req).execute().use {
                if (!it.isSuccessful) {
                    throw IOException("Erreur HTTP ${it.code} lors du téléchargement du fichier ZIP.")
                }
                return it.body?.bytes()
                    ?: throw ZipFileException("Le corps du fichier ZIP téléchargé est vide.")
            }
        } catch (e: ConnectException) {
            throw ConnectException("Erreur de connexion au serveur pour l'URL $url.").initCause(e)
        } catch (e: SocketTimeoutException) {
            throw SocketTimeoutException("Délai de connexion expiré pour l'URL $url.").initCause(e)
        } catch (e: IOException) {
            throw IOException("Erreur d'entrée/sortie lors du téléchargement de $url.", e)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun extraireUrlDepuisJson(json: String): String {
        try {
            val liste: List<Map<String, Any?>> = mapper.readValue(
                json,
                mapper.typeFactory.constructCollectionType(List::class.java, Map::class.java)
            )

            val premier = liste.firstOrNull()
                ?: throw DataFormatException("Le document JSON est vide ou ne contient pas d'objet valide.")

            return premier["url"] as? String
                ?: throw DataFormatException("Clé 'url' introuvable ou invalide dans le JSON.")
        } catch (e: JsonParseException) {
            throw DataFormatException("Erreur de format (parsing) du JSON reçu.", e)
        } catch (e: Exception) {
            throw DataFormatException("Erreur lors de l'extraction de l'URL du JSON.", e)
        }
    }

    private fun extraireFichiers(zipData: ByteArray) {
        val dossierGTFS = File(context.filesDir, "gtfs")

        if (!dossierGTFS.exists()) {
            dossierGTFS.mkdirs()
        }

        try {
            ZipInputStream(zipData.inputStream()).use { zip ->
                var entry: ZipEntry? = zip.nextEntry

                while (entry != null) {
                    val fileName = entry.name

                    if (fileName in fichiersCibles) {
                        println("Extraction du fichier : $fileName")
                        val outFile = File(dossierGTFS, fileName)

                        outFile
                            .outputStream()
                            .use { output -> zip.copyTo(output) }
                    }

                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
        catch (e: Exception) {
            throw ZipFileException("Erreur lors de l'extraction des fichiers du ZIP.", e)
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
        GlobalScope.launch(Dispatchers.Default) {
            try {
                val nm = NotificationManagerCompat.from(context)
                val builder = NotificationCompat.Builder(context, "IMPORT_CHANNEL")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(contenu)
                    .setContentText("")
                    .setPriority(NotificationCompat.PRIORITY_LOW)

                // Utiliser un notifId unique à chaque fois
                nm.notify(System.currentTimeMillis().toInt() and 0xfffffff, builder.build())
            } catch (e: Exception) {
                Log.e("IMPORT", "Erreur lors de la notification", e)
            }
        }
    }

}
