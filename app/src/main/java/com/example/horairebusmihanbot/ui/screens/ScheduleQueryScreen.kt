package com.example.horairebusmihanbot.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.repository.BusRouteRepository
import com.example.horairebusmihanbot.ui.components.AppDrawer
import com.example.horairebusmihanbot.ui.components.BusLineDropdown
import com.example.horairebusmihanbot.viewmodel.ChooseBusLineViewModel
import com.example.horairebusmihanbot.viewmodel.MainViewModele
import com.example.horairebusmihanbot.viewmodel.dependances.ChooseBusLineViewModelFactory
import java.time.format.DateTimeFormatter

/**
 * Vue permettant de sélectionner une ligne de bus et une date/heure (version Compose).
 */
@Composable
fun ScheduleQueryScreen(
    busRouteRepository: BusRouteRepository,
    mainViewModele: MainViewModele
) {

    // 2. Création de la Factory, mémorisée pour éviter de la recréer à chaque recomposition
    val factory = remember {
        ChooseBusLineViewModelFactory(busRouteRepository)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // 3. Récupération du ViewModel en utilisant la Factory personnalisée
    val viewModel: ChooseBusLineViewModel = viewModel(factory = factory)

    val busRoutes by viewModel.busRoutes.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTime by viewModel.selectedTime.collectAsState()
    val directions by viewModel.directions.collectAsState()

    val context = LocalContext.current

    // Formatters
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    // --- 2. Dialogues d'Affichage (Équivalent des fonctions showDatePicker/showTimePicker) ---

    // Dialogue Date (utilisation du DatePickerDialog Android)
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                viewModel.updateSelectedDate(year, month, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
    }

    // Dialogue Heure (utilisation du TimePickerDialog Android)
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                viewModel.updateSelectedTime(hourOfDay, minute)
            },
            selectedTime.hour,
            selectedTime.minute,
            true // Format 24h
        )
    }

    var isMenuEnabled by remember { mutableStateOf(true) }


    // --- 3. Construction de l'UI ---

    val content: @Composable (PaddingValues) -> Unit = { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {

            // --- SÉLECTION DATE ---
            Text(
                text = stringResource(R.string.label_selected_date),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedDate.format(dateFormatter))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SÉLECTION HEURE ---
            Text(
                text = stringResource(R.string.label_selected_time),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = { timePickerDialog.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedTime.format(timeFormatter))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SÉLECTION LIGNE DE BUS (Spinner/Dropdown) ---
            Text(
                text = stringResource(R.string.label_select_bus_line),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            BusLineDropdown(
                routes = busRoutes,
                selectedRoute = selectedRoute,
                onRouteSelected = viewModel::onRouteSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // --- DIRECTIONS DISPONIBLES (RecyclerView / LazyColumn) ---
            Text(
                text = stringResource(R.string.label_available_directions),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Utiliser weight pour prendre l'espace restant
            ) {
                items(directions) { direction ->
                    ListItem(
                        headlineContent = { Text(direction) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* TODO: Gérer la sélection de la direction */ }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    val lockClick: (action: () -> Unit) -> Unit = { action ->
        if (isMenuEnabled) {
            isMenuEnabled = false
            action()
        }
        // Note : C'est la responsabilité du MainViewModel de remettre
        // isMenuEnabled à 'true' après la fin des opérations longues.
    }

    AppDrawer(
        mainViewModele,
        drawerState,
        isMenuEnabled,
        lockClick,
        content
    )
}