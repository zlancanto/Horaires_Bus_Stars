package com.example.horairebusmihanbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.horairebusmihanbot.MainViewModele
import com.example.horairebusmihanbot.ui.theme.HoraireBusMihanBotTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    // Récupération du ViewModel
    private val viewModel: MainViewModele by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@Composable
fun ImportScreen(viewModel: MainViewModele) {
    // Observation des états du ViewModel
    val progress by viewModel.importProgress.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isImporting) {
            Box(contentAlignment = Alignment.Center) {
                // Barre circulaire (Progress)
                CircularProgressIndicator(
                    progress = { progress }, // Conversion de StateFlow en paramètre
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 8.dp,
                )
                // Texte du pourcentage au centre
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Importation des données...")
            Text("Veuillez patienter", style = MaterialTheme.typography.bodySmall)
        } else {
            if (progress >= 1f) {
                Text("Importation terminée avec succès !", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { viewModel.startGtfsImport() },
                modifier = Modifier.height(50.dp)
            ) {
                Text("Lancer l'importation des données")
            }
        }
    }
}