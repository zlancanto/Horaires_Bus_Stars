package com.example.horairebusmihanbot.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.horairebusmihanbot.MainApp
import com.example.horairebusmihanbot.repository.SyncRepository
import com.example.horairebusmihanbot.state.SyncState
import com.example.horairebusmihanbot.services.StarDataService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SyncViewModel : ViewModel() {
    private val repository = MainApp.repository
    val state = SyncRepository.state

    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    /**
     * Vérifie si les données existent déjà.
     * Si la base est vide, lance le service de téléchargement.
     */
    fun checkAndStartSync(context: Context) {
        viewModelScope.launch {
            // On vérifie en arrière-plan (Dispatchers.IO) pour ne pas figer l'UI
            val isEmpty = repository.database.isDatabaseEmpty()

            if (isEmpty) {
                SyncRepository.update(SyncState.Idle)
                val intent = Intent(context, StarDataService::class.java)
                context.startForegroundService(intent)
            } else {
                // Si les données sont déjà là, on informe le Repository pour naviguer
                SyncRepository.update(SyncState.Finished)
            }
        }
    }
}