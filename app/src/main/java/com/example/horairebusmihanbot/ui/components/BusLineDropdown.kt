package com.example.horairebusmihanbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.data.entity.BusRoute

/**
 * Composant d'Abstraction pour le Dropdown (équivalent du Spinner/Adapter)
 */
@Composable
fun BusLineDropdown(
    routes: List<BusRoute>,
    selectedRoute: BusRoute?,
    onRouteSelected: (BusRoute?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Définir les couleurs par défaut
    val defaultRouteColor = MaterialTheme.colorScheme.surface
    val defaultTextColor = MaterialTheme.colorScheme.onSurface

    // Le texte affiché dans le bouton
    val displayValue = selectedRoute?.shortName ?: stringResource(R.string.label_select_bus_line)

    // Couleurs du bouton actuellement sélectionné (si disponible)
    val selectedBgColor = gtfsColorToComposeColor(selectedRoute?.color, defaultRouteColor)
    val selectedTextColor = gtfsColorToComposeColor(selectedRoute?.textColor, defaultTextColor)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
    ) {
        OutlinedButton(
            onClick = { if (routes.isNotEmpty()) expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .background(selectedBgColor)
        ) {
            Text(
                displayValue,
                color = selectedTextColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown",
                tint = selectedTextColor
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f) // Prend 90% de la largeur du parent
        ) {
            routes.forEach { route ->
                val bgColor = gtfsColorToComposeColor(route.color, defaultRouteColor)
                val txtColor = gtfsColorToComposeColor(route.textColor, defaultTextColor)
                DropdownMenuItem(
                    text = { route.shortName?.let { Text(it, color = txtColor) } },
                    onClick = {
                        onRouteSelected(route)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                )
            }
        }
    }
}