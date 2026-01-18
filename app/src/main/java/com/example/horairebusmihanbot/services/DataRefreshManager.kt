package com.example.horairebusmihanbot.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.horairebusmihanbot.MainApp
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.repository.SyncRepository
import com.example.horairebusmihanbot.state.SyncState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataRefreshManager(private val context: Context) {
    private val database = MainApp.database

    fun showRefreshDialog(navController: NavController) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.db_refresh_dialogue_title)
            .setMessage(R.string.db_refresh_dialogue_content)
            .setNegativeButton(R.string.db_refresh_dialogue_cancel, null)
            .setPositiveButton(R.string.db_refresh_dialogue_confirm) { _, _ ->
                performReset(navController)
            }
            .show()
    }

    private fun performReset(navController: NavController) {
        var msg: String

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // On exécute la suppression sur le thread IO via le repository
                withContext(Dispatchers.IO) {
                    database.clearAllData()
                    SyncRepository.update(SyncState.Idle)
                }
                navController.navigate(R.id.action_global_to_sync)
            }
            catch (e: Exception) {
                msg = context.getString(R.string.db_refresh_error_delete)
                Log.e("DataRefreshManager", "Erreur lors du nettoyage", e)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}