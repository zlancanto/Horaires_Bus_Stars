package com.example.horairebusmihanbot.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.horairebusmihanbot.MainApp
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.repository.SyncRepository
import com.example.horairebusmihanbot.repository.SyncState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataRefreshManager(private val context: Context) {
    private val dao = MainApp.database.starDao()

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
        // On utilise un scope lié à l'application ou au manager pour ne pas être coupé
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Nettoyage
                dao.clearAllTables()

                // 2. Reset de l'état
                SyncRepository.update(SyncState.Idle)

                // 3. UI sur le thread principal
                withContext(Dispatchers.Main) {
                    navController.navigate(R.id.action_global_to_sync)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DataRefreshManager", "Erreur lors du nettoyage", e)
                    Toast.makeText(context, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}