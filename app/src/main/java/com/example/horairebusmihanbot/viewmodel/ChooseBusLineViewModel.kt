/*package com.example.horairebusmihanbot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.horairebusmihanbot.data.entity.BusRoute
import com.example.horairebusmihanbot.repository.BusRouteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class ChooseBusLineViewModel constructor(
    private val busRouteRepository: BusRouteRepository
) : ViewModel() {

    // Utiliser un StateFlow pour charger la liste une seule fois au démarrage
    private val _busRoutes = MutableStateFlow<List<BusRoute>>(emptyList())
    val busRoutes: StateFlow<List<BusRoute>> = _busRoutes.asStateFlow()

    private val _selectedRoute = MutableStateFlow<BusRoute?>(null)
    val selectedRoute: StateFlow<BusRoute?> = _selectedRoute.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedTime = MutableStateFlow(LocalTime.now())
    val selectedTime: StateFlow<LocalTime> = _selectedTime.asStateFlow()

    // Récupérer les directions correspondantes.
    @OptIn(ExperimentalCoroutinesApi::class)
    val directions: StateFlow<List<String>> = _selectedRoute
        .flatMapLatest { route ->
            if (route == null) {
                // Si aucune route sélectionnée, émettre une liste vide
                flow { emit(emptyList()) }
            } else {
                // Utiliser un flow builder pour lancer la requête suspendue
                flow {
                    val result = busRouteRepository.getDirectionsByRoute(route.id)
                    emit(result)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Chargement initial des lignes de bus (doit être fait dans le Repository)
        viewModelScope.launch {
            busRouteRepository.getAllAsFlow().collect { routes ->
                _busRoutes.value = routes
                if (routes.isNotEmpty() && _selectedRoute.value == null) {
                    _selectedRoute.value = routes.first() // Sélectionner la première par défaut
                }
            }
        }
    }

    fun onRouteSelected(route: BusRoute?) {
        _selectedRoute.value = route
    }

    fun updateSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        _selectedDate.value = LocalDate.of(year, month + 1, dayOfMonth)
    }

    fun updateSelectedTime(hour: Int, minute: Int) {
        _selectedTime.value = LocalTime.of(hour, minute)
    }
}*/