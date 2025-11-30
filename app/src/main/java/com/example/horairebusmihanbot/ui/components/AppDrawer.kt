package com.example.horairebusmihanbot.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.viewmodel.MainViewModele
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Composant Modulaire représentant la structure de navigation principale (Drawer + Scaffold).
 * Ce composant gère l'état du tiroir et encapsule l'AppDrawerContent.
 *
 * @param mainViewModel Le ViewModel gérant les opérations de base de données/téléchargement.
 * @param drawerState L'état actuel du tiroir (ou l'état à utiliser).
 * @param isMenuEnabled L'état de l'interface utilisateur pour verrouiller les clics pendant les opérations.
 * @param lockClick La fonction lambda pour gérer le verrouillage/déverrouillage de l'UI.
 * @param content Le contenu de l'écran principal affiché à l'intérieur du Scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    mainViewModel: MainViewModele,
    drawerState: DrawerState,
    isMenuEnabled: Boolean,
    lockClick: (() -> Unit) -> Unit,
    snackbarHostState: SnackbarHostState,
    content: @Composable (PaddingValues) -> Unit
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                AppDrawerContent(
                    mainViewModel = mainViewModel,
                    drawerState = drawerState,
                    scope = scope,
                    isMenuEnabled = isMenuEnabled,
                    lockClick = lockClick
                )
            }
        },
        content = {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.app_name)) },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                },
                content = content
            )
        }
    )
}