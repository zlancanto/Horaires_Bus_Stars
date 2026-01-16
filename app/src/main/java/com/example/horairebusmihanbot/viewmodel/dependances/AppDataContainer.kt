/*package com.example.horairebusmihanbot.viewmodel.dependances

import android.content.Context
import com.example.horairebusmihanbot.data.AppDatabase
import com.example.horairebusmihanbot.data.impl.BusRouteImpl // Importation de l'implémentation
import com.example.horairebusmihanbot.repository.BusRouteRepository

/**
 * Conteneur simple pour gérer les dépendances de l'application.
 *
 * Dans une application réelle, ceci serait géré par Hilt ou Koin.
 * Ici, nous initialisons le Repository avec l'instance de la base de données.
 */
interface AppContainer {
    val busRouteRepository: BusRouteRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val busRouteRepository: BusRouteRepository by lazy {
        // Utilisation du pattern Builder de BusRouteImpl pour l'initialisation
        BusRouteImpl.Builder()
            .busRouteDao(database.busRouteDao())
            .tripDao(database.tripDao()) // Ajout de la dépendance TripDao
            .build()
    }
}*/