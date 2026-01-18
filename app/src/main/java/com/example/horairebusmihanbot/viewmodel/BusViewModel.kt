package com.example.horairebusmihanbot.viewmodel

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.horairebusmihanbot.MainApp
import com.example.horairebusmihanbot.dto.StopTimeWithLabelDto
import com.example.horairebusmihanbot.model.*
import com.example.horairebusmihanbot.utils.getDayOfWeekColumn
import com.example.horairebusmihanbot.utils.isSameDay
import kotlinx.coroutines.launch
import java.util.Locale

class BusViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = MainApp.database.starDao()

    private val _selectedDateTime = MutableLiveData<Calendar>()
    val selectedDateTime: LiveData<Calendar> = _selectedDateTime

    // Données observées par les fragments
    val allRoutes: LiveData<List<BusRoute>> = dao.getAllRoutes()

    private val _directions = MutableLiveData<List<DirectionInfo>>()
    val directions: LiveData<List<DirectionInfo>> = _directions

    private val _stops = MutableLiveData<List<Stop>>()
    val stops: LiveData<List<Stop>> = _stops

    private val _selectedRoute = MutableLiveData<BusRoute?>(null)
    val selectedRoute: LiveData<BusRoute?> = _selectedRoute

    private val _stopTimes = MutableLiveData<List<StopTime>>()
    val stopTimes: LiveData<List<StopTime>> = _stopTimes

    private val _tripDetails = MutableLiveData<List<StopTimeWithLabelDto>>()
    val tripDetails: LiveData<List<StopTimeWithLabelDto>> = _tripDetails

    // Fonctions appelées par les fragments

    fun setSelectedRoute(route: BusRoute?) {
        _selectedRoute.value = route
    }

    fun setSelectedDate(timeInMillis: Long) {
        val now = Calendar.getInstance()
        val current = _selectedDateTime.value ?: now
        val newDate = Calendar.getInstance().apply { this.timeInMillis = timeInMillis }

        current.set(Calendar.YEAR, newDate.get(Calendar.YEAR))
        current.set(Calendar.MONTH, newDate.get(Calendar.MONTH))
        current.set(Calendar.DAY_OF_MONTH, newDate.get(Calendar.DAY_OF_MONTH))

        _selectedDateTime.value = current
    }

    fun setSelectedTime(hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val current = _selectedDateTime.value ?: now

        // Création d'un calendrier de test
        val testCal = (current.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        // Validation : Si c'est aujourd'hui et que c'est avant maintenant, on refuse
        if (isSameDay(testCal, now) && testCal.before(now)) {
            // On force l'heure actuelle
            current.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
            current.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
        } else {
            current.set(Calendar.HOUR_OF_DAY, hour)
            current.set(Calendar.MINUTE, minute)
        }

        _selectedDateTime.value = current
    }

    fun loadDirections(routeId: String) {
        viewModelScope.launch {
            _directions.value = dao.getDirections(routeId)
        }
    }

    fun loadStops(routeId: String, dirId: Int) {
        viewModelScope.launch {
            _stops.value = dao.getStops(routeId, dirId)
        }
    }

    fun loadTripDetails(tripId: String, stopSequence: Int) {
        viewModelScope.launch {
            _tripDetails.value = dao.getTripDetails(tripId, stopSequence)
        }
    }

    fun loadNextPassages(routeId: String, stopId: String, directionId: Int) {
        val calendar = _selectedDateTime.value ?: Calendar.getInstance()

        // Formatage pour la base de données
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentTimeStr = sdf.format(calendar.time)
        val dayColumn = getDayOfWeekColumn(calendar)

        viewModelScope.launch {
            try {
                val result = dao.getNextPassages(
                    stopId,
                    routeId,
                    directionId,
                    currentTimeStr,
                    dayColumn
                )
                _stopTimes.postValue(result)
            } catch (e: Exception) {
                Log.e("BusViewModel", "Erreur SQL : ${e.message}")
                _stopTimes.postValue(emptyList())
            }
        }
    }

    /**
     * Helper pour récupérer le timestamp final nécessaire aux requêtes SQL
     */
    fun getSelectedTimestamp(): Long {
        return _selectedDateTime.value?.timeInMillis ?: System.currentTimeMillis()
    }
}