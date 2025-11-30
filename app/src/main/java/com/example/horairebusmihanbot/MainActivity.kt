package com.example.horairebusmihanbot

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.horairebusmihanbot.ui.screens.ImportScreen
import com.example.horairebusmihanbot.ui.screens.ScheduleQueryScreen
import com.example.horairebusmihanbot.ui.theme.HoraireBusMihanBotTheme
import com.example.horairebusmihanbot.viewmodel.MainViewModele
import com.example.horairebusmihanbot.viewmodel.dependances.AppContainer
import com.example.horairebusmihanbot.viewmodel.dependances.AppDataContainer
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    // 1. Initialiser le conteneur de dépendances au niveau de l'application
    private val appContainer: AppContainer by lazy { AppDataContainer(applicationContext) }

    private val viewModel: MainViewModele by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
            if (checkSelfPermission(android.Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.INTERNET),
                    1
                )
            }
        }

        super.onCreate(savedInstanceState)
        createNotificationChannel()

        val gtfsFolder = File(filesDir, "gtfs")
        val stopsFile = File(gtfsFolder, "stops.txt")
        Log.e("VERIF", " ${stopsFile.exists()}")
        if (!gtfsFolder.exists() || !stopsFile.exists()) {
            lifecycleScope.launch {
                viewModel.clearDatabase()            // attend la fin
                viewModel.telechargerFichiersval()   // attend la fin
                viewModel.startGtfsImport()          // attend la fin
            }
        }

        setContent {
            HoraireBusMihanBotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Collecter l'état qui détermine si l'importation est terminée
                    val isImportComplete by viewModel.isImportComplete.collectAsState() // Supposer un Flow<Boolean> dans ViewModel

                    if (isImportComplete) {
                        ScheduleQueryScreen(
                            busRouteRepository = appContainer.busRouteRepository,
                            viewModel
                        )
                    } else {
                        ImportScreen(viewModel)
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "IMPORT_CHANNEL",
                "Import",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}