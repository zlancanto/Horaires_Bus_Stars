package com.example.horairebusmihanbot

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.horairebusmihanbot.data.*
import com.example.horairebusmihanbot.data.repository.*
import com.example.horairebusmihanbot.services.RenseignerBaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModele(application: Application) : AndroidViewModel(application) {

    // État de l'importation
    // -1f = pas commencé, 0f-1f = en cours, 2f = terminé
    private val _importProgress = MutableStateFlow(-1f)
    val importProgress: StateFlow<Float> = _importProgress.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    fun startGtfsImport() {
        if (_isImporting.value) return

        _isImporting.value = true
        _importProgress.value = 0f

        viewModelScope.launch {
            // Initialisation manuelle des dépendances (Idéalement utiliser Hilt/Koin)
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
                _importProgress.value = 1f // Assurer que c'est fini
            } catch (e: Exception) {
                e.printStackTrace()
                _importProgress.value = -1f // Erreur / Reset
            } finally {
                _isImporting.value = false
            }
        }
    }
}