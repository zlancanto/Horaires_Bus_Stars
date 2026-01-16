package com.example.horairebusmihanbot.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SyncRepository {
    // StateFlow pour observer la progression (0 à 100)
    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    // Indique si la base est prête
    private val _isDataReady = MutableStateFlow(false)
    val isDataReady: StateFlow<Boolean> = _isDataReady.asStateFlow()

    fun updateProgress(value: Int) {
        _progress.value = value
    }

    fun setDataReady(ready: Boolean) {
        _isDataReady.value = ready
    }
}