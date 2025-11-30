package com.example.horairebusmihanbot.viewmodel.dependances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.horairebusmihanbot.repository.BusRouteRepository
import com.example.horairebusmihanbot.viewmodel.ChooseBusLineViewModel

/**
 * Factory personnalisée pour instancier ChooseBusLineViewModel.
 *
 * Elle est nécessaire car ChooseBusLineViewModel prend BusRouteRepository comme argument,
 * et le système ViewModel par défaut ne sait pas comment fournir cette dépendance.
 */
class ChooseBusLineViewModelFactory(
    private val repository: BusRouteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChooseBusLineViewModel::class.java)) {
            return ChooseBusLineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}