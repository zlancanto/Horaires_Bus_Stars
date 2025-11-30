package com.example.horairebusmihanbot.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.viewmodel.MainViewModele
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Contenu du tiroir de navigation. Peut être utilisé séparément si le ModalNavigationDrawer
 * est géré à un niveau supérieur.
 */
@Composable
fun AppDrawerContent(
    mainViewModel: MainViewModele,
    drawerState: DrawerState,
    scope: CoroutineScope,
    isMenuEnabled: Boolean,
    lockClick: (() -> Unit) -> Unit
) {
    // La variable isMenuEnabled est passée par valeur, donc nous devons utiliser la
    // fonction lockClick pour gérer son changement d'état via le composant parent.

    Spacer(Modifier.height(24.dp))
    Text(
        stringResource(R.string.menu),
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.titleLarge
    )

    // 1. Retélécharger la base (Clear -> Download -> Import)
    NavigationDrawerItem(
        label = { Text(stringResource(R.string.reload_database)) },
        selected = false,
        modifier = Modifier.alpha(if (isMenuEnabled) 1f else 0.5f),
        onClick = {
            lockClick {
                scope.launch {
                    drawerState.close()
                    mainViewModel.clearDatabase()
                    mainViewModel.telechargerFichiers()
                    mainViewModel.startGtfsImport()
                }
            }
        }
    )
}