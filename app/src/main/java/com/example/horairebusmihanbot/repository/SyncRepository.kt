package com.example.horairebusmihanbot.repository

import com.example.horairebusmihanbot.state.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SyncRepository {
    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state = _state.asStateFlow()

    fun update(newState: SyncState) { _state.value = newState }
}