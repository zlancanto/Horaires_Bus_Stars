package com.example.horairebusmihanbot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.ui.components.AppDrawer
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isImporting) {
        if (!isImporting) isMenuEnabled = true
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Fonction utilitaire : empêche le clic si désactivé
    val lockClick: (action: () -> Unit) -> Unit = { action ->
        if (isMenuEnabled) {
            isMenuEnabled = false
            action()
        }
        // Note : C'est la responsabilité du MainViewModel de remettre
        // isMenuEnabled à 'true' après la fin des opérations longues.
    }

    val content: @Composable (PaddingValues) -> Unit = { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                Text(stringResource(R.string.state_download_in_progress))
                Text(
                    stringResource(R.string.please_wait),
                    style = MaterialTheme.typography.bodySmall
                )

            }

            if (databaseCleared) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.database_successfully_emptied),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    AppDrawer(
        viewModel,
        drawerState,
        isMenuEnabled,
        lockClick,
        snackbarHostState,
        content
    )
}
