package com.example.horairebusmihanbot.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.horairebusmihanbot.data.entity.BusRoute
import com.example.horairebusmihanbot.repository.BusRouteRepository
import java.time.LocalDate
import java.time.LocalTime

class ChooseBusLineViewModel constructor(
    private val busRouteRepository: BusRouteRepository
) : ViewModel() {
    // Lignes de Bus (pour le Spinner)
    val busRoutes: LiveData<List<BusRoute>> = busRouteRepository.getAll()

    private val _selectedRoute = MutableLiveData<BusRoute?>()
    val selectedRoute: LiveData<BusRoute?> = _selectedRoute

    private val _selectedDate = MutableLiveData(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    private val _selectedTime = MutableLiveData(LocalTime.now())
    val selectedTime: LiveData<LocalTime> = _selectedTime

    // pour récupérer les directions correspondantes.
    val directions: LiveData<List<String>> = _selectedRoute.switchMap { route ->
        if (route == null) {
            MutableLiveData(emptyList())
        } else {
            // Utiliser liveData builder pour lancer la requête suspendue
            liveData(viewModelScope.coroutineContext) {
                val result = busRouteRepository.getDirectionsByRoute(route.id)
                emit(result)
            }
        }
    }

    //Logique d'interaction
    fun onRouteSelected(route: BusRoute?) {
        _selectedRoute.value = route
    }

    fun updateSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        _selectedDate.value = LocalDate.of(year, month + 1, dayOfMonth)
    }

    fun updateSelectedTime(hour: Int, minute: Int) {
        _selectedTime.value = LocalTime.of(hour, minute)
    }
}