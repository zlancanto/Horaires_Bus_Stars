package com.example.horairebusmihanbot

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.horairebusmihanbot.data.*
import com.example.horairebusmihanbot.data.repository.*
import com.example.horairebusmihanbot.services.RenseignerBaseService
import com.example.horairebusmihanbot.services.TelechargerFichiersService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModele(application: Application) : AndroidViewModel(application) {

    // État de l'importation
    // -1f = pas commencée, 0f-1f = en cours, 2f = terminée
    private val _databaseCleared = MutableStateFlow(false)
    val databaseCleared = _databaseCleared.asStateFlow()

    private val _importProgress = MutableStateFlow(-1f)
    val importProgress: StateFlow<Float> = _importProgress.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    suspend fun startGtfsImport() {
        if (_isImporting.value) return
        Log.e("IMPORT", "Appel import")
        _isImporting.value = true
        _importProgress.value = 0f

        viewModelScope.launch {
            // Initialisation manuelle des dépendances
            val context = getApplication<Application>().applicationContext
            val db = AppDatabase.getDatabase(context)

            val service = RenseignerBaseService(
                context,
                BusRouteRepository(db.busRouteDao()),
                CalendarRepository(db.calendarDao()),
                StopRepository(db.stopDao()),
                TripRepository(db.tripDao()),
                StopTimeRepository(db.stopTimeDao())
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
            }

        }
    }

    suspend fun clearDatabase() {
        val context = getApplication<Application>().applicationContext
        val db = AppDatabase.getDatabase(context)
        val service = RenseignerBaseService(
            context,
            BusRouteRepository(db.busRouteDao()),
            CalendarRepository(db.calendarDao()),
            StopRepository(db.stopDao()),
            TripRepository(db.tripDao()),
            StopTimeRepository(db.stopTimeDao())
        )

        service.clearDatabase()

        _databaseCleared.value = true
        _isImporting.value = false
    }

    suspend fun telechargerFichiersval() {
        var service = TelechargerFichiersService( getApplication<Application>().applicationContext)
        withContext(Dispatchers.IO) {
            service.telechargerEtExtraire()
        }
    }

}