package com.example.horairebusmihanbot.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SyncRepository {
    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state = _state.asStateFlow()

    fun update(newState: SyncState) { _state.value = newState }

    ///////
    private val _progress = MutableStateFlow(0)
    val progress = _progress.asStateFlow()

    private val _currentTable = MutableStateFlow("")
    val currentTable = _currentTable.asStateFlow()

    private val _isDataReady = MutableStateFlow(false)
    val isDataReady = _isDataReady.asStateFlow()

    fun updateProgress(value: Int, tableName: String = "") {
        _progress.value = value
        if (tableName.isNotEmpty()) _currentTable.value = tableName
    }

    fun setDataReady(ready: Boolean) {
        _isDataReady.value = ready
    }
}