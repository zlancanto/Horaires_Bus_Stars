package com.example.horairebusmihanbot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.horairebusmihanbot.viewmodel.MainViewModele
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(viewModel: MainViewModele) {

    val progress by viewModel.importProgress.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val databaseCleared by viewModel.databaseCleared.collectAsState()

    var isMenuEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(isImporting) {
        if (!isImporting) isMenuEnabled = true
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Fonction utilitaire : empêche le clic si désactivé
    fun lockClick(action: () -> Unit) {
        if (isMenuEnabled) {
            isMenuEnabled = false
            action()
        }
    }

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
                // Retélécharger la base
                NavigationDrawerItem(
                    label = { Text("Retélécharger la base") },
                    selected = false,
                    modifier = Modifier.alpha(if (isMenuEnabled) 1f else 0.5f),
                    onClick = {
                        lockClick {
                            scope.launch {
                                drawerState.close()
                                viewModel.clearDatabase()            // attend la fin
                                viewModel.telechargerFichiersval()
                                // attend la fin
                                viewModel.startGtfsImport()
                            }
                        }
                    }
                )
                // Importer les données
                NavigationDrawerItem(
                    label = { Text("Importer les données") },
                    selected = false,
                    modifier = Modifier.alpha(if (isMenuEnabled) 1f else 0.5f),
                    onClick = {
                        lockClick {
                            scope.launch { drawerState.close()
                                viewModel.startGtfsImport()
                            }
                        }
                    }
                )

                // Vider la base
                NavigationDrawerItem(
                    label = { Text("Vider la base de données") },
                    selected = false,
                    modifier = Modifier.alpha(if (isMenuEnabled) 1f else 0.5f),
                    onClick = {
                        lockClick {
                            scope.launch { drawerState.close()
                                viewModel.clearDatabase()
                            }
                            isMenuEnabled = true // Action rapide
                        }
                    }
                )

                // Télécharger les données
                NavigationDrawerItem(
                    label = { Text("Télécharger les fichiers") },
                    selected = false,
                    modifier = Modifier.alpha(if (isMenuEnabled) 1f else 0.5f),
                    onClick = {
                        lockClick {
                            scope.launch { drawerState.close()
                                viewModel.telechargerFichiersval()
                            }
                            isMenuEnabled = true // Action rapide
                        }
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
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
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

                } else if (progress >= 1f) {
                    Text(
                        "Importation terminée avec succès !",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

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
