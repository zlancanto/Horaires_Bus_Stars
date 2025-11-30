/*package com.example.horairebusmihanbot.vue

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.horairebusmihanbot.data.entity.BusRoute
import com.example.horairebusmihanbot.databinding.ChooseBusLineBinding
import com.example.horairebusmihanbot.viewmodel.ChooseBusLineViewModel
import com.example.horairebusmihanbot.vue.adapter.BusRouteAdapter
import com.example.horairebusmihanbot.vue.adapter.DirectionsAdapter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Vue permettant de sélectionner une ligne de bus et une date/heure.
 */
class ChooseBusLineView : Fragment() {

    private var _binding: ChooseBusLineBinding? = null
    private val binding get() = _binding!!

    // Utilisation de 'by viewModels()' pour initialiser le ViewModel.
    private val viewModel: ChooseBusLineViewModel by viewModels()

    private lateinit var busRouteAdapter: BusRouteAdapter
    private lateinit var directionsAdapter: DirectionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ChooseBusLineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupListeners()
        setupObservers()
    }

    /**
     * Initialisation des Adapteurs.
     */
    private fun setupAdapters() {
        // Adaptateur pour le Spinner
        busRouteAdapter = BusRouteAdapter(requireContext(), mutableListOf())
        binding.spinnerBusLines.adapter = busRouteAdapter

        // Adaptateur pour les Directions
        directionsAdapter = DirectionsAdapter(mutableListOf())
        binding.recyclerDirections.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = directionsAdapter
        }
    }

    /**
     * Initialisation des Listeners.
     */
    private fun setupListeners() {
        // Sélecteurs de Date et d'Heure
        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSelectTime.setOnClickListener { showTimePicker() }

        // Spinner de lignes de bus
        binding.spinnerBusLines.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override
            fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // S'assurer que la liste n'est pas vide avant de tenter de convertir
                if (parent != null && parent.adapter.count > 0) {
                    val selectedRoute = parent.getItemAtPosition(position) as BusRoute
                    viewModel.onRouteSelected(selectedRoute)
                }
            }
            override
            fun onNothingSelected(parent: AdapterView<*>?) {
                // Optionnel : Gérer l'état où aucune ligne n'est sélectionnée
                viewModel.onRouteSelected(null)
            }
        }
    }

    /**
     * Observers de route de bus, date de date et de l'heure.
     */
    private fun setupObservers() {
        // Observer la liste des lignes de bus et mettre à jour le Spinner
        viewModel.busRoutes.observe(viewLifecycleOwner) { routes ->
            busRouteAdapter.updateRoutes(routes)
            // Sélectionner la première ligne par défaut si la liste est remplie
            if (routes.isNotEmpty() && binding.spinnerBusLines.selectedItem == null) {
                binding.spinnerBusLines.setSelection(0)
            }
        }

        // Observer la date et mettre à jour le bouton
        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            binding.btnSelectDate.text = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        }

        // Observer l'heure et mettre à jour le bouton
        viewModel.selectedTime.observe(viewLifecycleOwner) { time ->
            binding.btnSelectTime.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        // Observer la liste des directions et mettre à jour la RecyclerView
        viewModel.directions.observe(viewLifecycleOwner) { directions ->
            directionsAdapter.updateDirections(directions)
        }
    }

    /**
     * Affichage du la date.
     */
    private fun showDatePicker() {
        val now = viewModel.selectedDate.value ?: LocalDate.now()
        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                viewModel.updateSelectedDate(year, month, dayOfMonth)
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        )
        dialog.show()
    }

    /**
     * Affichage de l'heure.
     */
    private fun showTimePicker() {
        val now = viewModel.selectedTime.value ?: LocalTime.now()
        val dialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                viewModel.updateSelectedTime(hourOfDay, minute)
            },
            now.hour,
            now.minute,
            true
        )
        dialog.show()
    }

    /**
     * Libération des ressources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}*/