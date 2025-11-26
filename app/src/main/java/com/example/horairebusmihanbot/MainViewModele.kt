package com.example.horairebusmihanbot

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.horairebusmihanbot.data.*
import com.example.horairebusmihanbot.data.impl.*
import com.example.horairebusmihanbot.services.RenseignerBaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModele(application: Application) : AndroidViewModel(application) {

    // État de l'importation
    // -1f = pas commencée, 0f-1f = en cours, 2f = terminée
    private val _databaseCleared = MutableStateFlow(false)
    val databaseCleared = _databaseCleared.asStateFlow()

    private val _importProgress = MutableStateFlow(-1f)
    val importProgress: StateFlow<Float> = _importProgress.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    fun startGtfsImport() {
        if (_isImporting.value) return

        _isImporting.value = true
        _importProgress.value = 0f

        viewModelScope.launch {
            // Initialisation manuelle des dépendances
            val context = getApplication<Application>().applicationContext
            val db = AppDatabase.getDatabase(context)

            val service = RenseignerBaseService(
                context,
                BusRouteImpl
                    .Builder()
                    .busRouteDao(db.busRouteDao())
                    .build(),
                CalendarImpl(db.calendarDao()),
                StopImpl(db.stopDao()),
                TripImpl(db.tripDao()),
                StopTimeImpl(db.stopTimeDao())
            )

            try {
                service.startImport { progress ->
                    _importProgress.value = progress
                }
                _importProgress.value = 1f
            } catch (e: Exception) {
                _importProgress.value = -1f
            } finally {
                _isImporting.value = false
                _databaseCleared.value = false
                sendNotification(context)
            }

        }
    }

    fun clearDatabase() {
        val context = getApplication<Application>().applicationContext
        val db = AppDatabase.getDatabase(context)
        val service = RenseignerBaseService(
            context,
            BusRouteImpl
                .Builder()
                .busRouteDao(db.busRouteDao())
                .build(),
            CalendarImpl(db.calendarDao()),
            StopImpl(db.stopDao()),
            TripImpl(db.tripDao()),
            StopTimeImpl(db.stopTimeDao())
        )

        viewModelScope.launch {
            service.clearDatabase()
        }
        _databaseCleared.value = true
        _isImporting.value = false
    }

    // Fonction qui envoie la notification en fin d'import.
    // Peut-être passer le texte en param pour refacto un peu lors de l'appel de la fonction pour le download.
    private fun sendNotification(context: Context) {
        // Vérification des permissions
        val hasPermission = ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS // on a la perm
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            android.util.Log.e("MainViewModele", "Pas de permission de notif")
            return
        }
        val nm = NotificationManagerCompat.from(context)
        val channel = nm.getNotificationChannel("IMPORT_CHANNEL")

        android.util.Log.d("MainViewModele", "Le channel existe = ${channel != null}")

        val builder = NotificationCompat.Builder(context, "IMPORT_CHANNEL")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Importation terminée")
            .setContentText("Données copiées dans la BDD.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        try {
            nm.notify(1, builder.build())
            android.util.Log.d("MainViewModele", "Notification envoyée !")
        } catch (e: Exception) {
            android.util.Log.e("MainViewModele", "Erreur lors de l'envoi de la notification", e)
        }
    }

}