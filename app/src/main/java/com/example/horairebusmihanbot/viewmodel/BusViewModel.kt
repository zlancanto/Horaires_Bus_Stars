package com.example.horairebusmihanbot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.horairebusmihanbot.MainApp
import com.example.horairebusmihanbot.model.*
import kotlinx.coroutines.launch

class BusViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = MainApp.database.starDao()

    // Données observées par les fragments
    val allRoutes: LiveData<List<BusRoute>> = dao.getAllRoutes()

    private val _directions = MutableLiveData<List<DirectionInfo>>()
    val directions: LiveData<List<DirectionInfo>> = _directions

    private val _stops = MutableLiveData<List<Stop>>()
    val stops: LiveData<List<Stop>> = _stops

    // Fonctions appelées par les fragments
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
}