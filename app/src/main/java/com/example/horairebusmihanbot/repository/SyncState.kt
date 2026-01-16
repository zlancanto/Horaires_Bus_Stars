package com.example.horairebusmihanbot.repository

sealed class SyncState {
    object Idle : SyncState()
    data class Progress(val percent: Int, val message: String) : SyncState()
    object Finished : SyncState()
    data class Error(val message: String) : SyncState()
}