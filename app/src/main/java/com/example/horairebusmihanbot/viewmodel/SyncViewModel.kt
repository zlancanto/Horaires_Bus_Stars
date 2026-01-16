package com.example.horairebusmihanbot.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.horairebusmihanbot.MainApp
import com.example.horairebusmihanbot.repository.SyncRepository
import com.example.horairebusmihanbot.services.StarDataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncViewModel : ViewModel() {
    val state = SyncRepository.state
    val progress = SyncRepository.progress
    val isDataReady = SyncRepository.isDataReady

    /**
     * Vérifie si les données existent déjà.
     * Si la base est vide, lance le service de téléchargement.
     */
    fun checkAndStartSync(context: Context) {
        viewModelScope.launch {
            // On vérifie en arrière-plan (Dispatchers.IO) pour ne pas figer l'UI
            val isEmpty = withContext(Dispatchers.IO) {
                // On vérifie par exemple si la table des routes est vide
                MainApp.database.starDao().isDatabaseEmpty()
            }

            if (isEmpty) {
                // Lancement du Service (Pattern Command)
                val intent = Intent(context, StarDataService::class.java)
                context.startForegroundService(intent)
            } else {
                // Si les données sont déjà là, on informe le Repository pour naviguer
                SyncRepository.setDataReady(true)
            }
        }
    }

    fun onStartSync(context: Context) {
        // 1. On s'assure que l'état est à "false" avant de commencer
        SyncRepository.setDataReady(false)

        // 2. Lancement du service
        val intent = Intent(context, StarDataService::class.java)
        context.startForegroundService(intent)
    }
}