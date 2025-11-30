package com.example.horairebusmihanbot.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

/**
 * Convertit une chaîne hexadécimale RRGGBB (sans #) en Color de Compose.
 * Gère les valeurs nulles ou invalides en retournant une couleur par défaut.
 */
@Composable
fun gtfsColorToComposeColor(hexColor: String?, defaultColor: Color): Color {
    if (hexColor.isNullOrBlank()) return defaultColor

    // Assure que le format est RRGGBB ou AARRGGBB
    val fullHex = if (hexColor.length == 6) "FF$hexColor" else hexColor

    return try {
        // La fonction `parseColor` de la classe Android Color est la plus fiable ici.
        // On la convertit ensuite en Color de Compose.
        Color("#$fullHex".toColorInt())
    } catch (_: IllegalArgumentException) {
        defaultColor
    }
}