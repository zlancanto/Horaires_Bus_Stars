package com.example.horairebusmihanbot

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.horairebusmihanbot.MainViewModele
import com.example.horairebusmihanbot.ui.theme.HoraireBusMihanBotTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    // Récupération du ViewModel
    private val viewModel: MainViewModele by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Demande la permission d'envoyer des notifications, obligatoire.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        // Channel de notification
        fun createNotificationChannel() {
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
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            HoraireBusMihanBotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImportScreen(viewModel)
                }
            }


        }


    }
}

// Menu latéral (joli) (ouaw)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(viewModel: MainViewModele) {
    val progress by viewModel.importProgress.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val databaseCleared by viewModel.databaseCleared.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                NavigationDrawerItem(
                    label = { Text("Importer les données") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.startGtfsImport()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Vider la base de données") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.clearDatabase()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Télécharger les données (TODO)") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Placeholder 2") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Horaire Bus Mihan") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (isImporting) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(100.dp),
                            strokeWidth = 8.dp
                        )
                        Text(
                            text = "${(progress * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Importation des données...")
                    Text("Veuillez patienter", style = MaterialTheme.typography.bodySmall)

                } else {
                    if (progress >= 1f) {
                        Text(
                            "Importation terminée avec succès !",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                // Vérifier que le message s'affiche correctement
                if (databaseCleared) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Base de données vidée avec succès !",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
