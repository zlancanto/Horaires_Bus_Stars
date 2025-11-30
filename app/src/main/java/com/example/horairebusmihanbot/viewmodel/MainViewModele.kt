package com.example.horairebusmihanbot.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.horairebusmihanbot.data.AppDatabase
import com.example.horairebusmihanbot.data.impl.BusRouteImpl
import com.example.horairebusmihanbot.data.impl.CalendarImpl
import com.example.horairebusmihanbot.data.impl.StopImpl
import com.example.horairebusmihanbot.data.impl.StopTimeImpl
import com.example.horairebusmihanbot.data.impl.TripImpl
import com.example.horairebusmihanbot.services.RenseignerBaseService
import com.example.horairebusmihanbot.services.TelechargerFichiersService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModele(application: Application) : AndroidViewModel(application) {

    private val initialImportStatus: Boolean = checkInitialImportStatus(application)
    private val _databaseCleared = MutableStateFlow(false)
    val databaseCleared = _databaseCleared.asStateFlow()

    private val _importProgress = MutableStateFlow(-1f)
    val importProgress: StateFlow<Float> = _importProgress.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _isImportComplete = MutableStateFlow(initialImportStatus)
    val isImportComplete: StateFlow<Boolean> = _isImportComplete.asStateFlow()

    fun startGtfsImport() {
        if (_isImporting.value) return

        _isImportComplete.value = false

        Log.e("IMPORT", "Appel import")
        _isImporting.value = true
        _importProgress.value = 0f

        viewModelScope.launch {
            // Initialisation manuelle des dépendances
            val context = getApplication<Application>().applicationContext
            val db = AppDatabase.Companion.getDatabase(context)

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
                _isImportComplete.value = true
            } catch (_: Exception) {
                _importProgress.value = -1f
            } finally {
                _isImporting.value = false
                _databaseCleared.value = false
            }

        }
    }

    suspend fun clearDatabase() {
        _isImportComplete.value = false

        val context = getApplication<Application>().applicationContext
        val db = AppDatabase.Companion.getDatabase(context)
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

        service.clearDatabase()

        _databaseCleared.value = true
        _isImporting.value = false
    }

    suspend fun telechargerFichiersval() {
        _isImportComplete.value = false

        val service = TelechargerFichiersService(getApplication<Application>().applicationContext)
        withContext(Dispatchers.IO) {
            service
                .telechargerEtExtraire()
                .onSuccess { }
                .onFailure { }
        }
    }

    private fun checkInitialImportStatus(application: Application): Boolean {
        // La même logique de vérification que dans MainActivity
        val context = application.applicationContext
        val gtfsFolder = File(context.filesDir, "gtfs")
        val stopsFile = File(gtfsFolder, "stops.txt")

        // Si les fichiers existent, on considère que l'importation est complète
        return gtfsFolder.exists() && stopsFile.exists()
    }
}