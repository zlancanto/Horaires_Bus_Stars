package com.example.horairebusmihanbot.ui

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.databinding.FragmentSelectionBinding
import com.example.horairebusmihanbot.model.BusRoute
import com.example.horairebusmihanbot.viewmodel.BusViewModel
import androidx.core.graphics.toColorInt
import java.util.Locale

class SelectionFragment : Fragment(R.layout.fragment_selection) {
    private val viewModel: BusViewModel by activityViewModels()

    // Flag pour éviter la boucle infinie lors de la restauration
    private var isRestoringState = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Crucial pour la stabilité
        val binding = FragmentSelectionBinding.bind(view)

        setupDateTimeSection(binding)
        setupBusSection(binding)
    }

    private fun setupDateTimeSection(binding: FragmentSelectionBinding) {
        val today = Calendar.getInstance().timeInMillis
        binding.datePicker.minDate = today

        // 1. Toggles d'affichage
        binding.btnDate.setOnClickListener {
            binding.datePicker.visibility = if (binding.datePicker.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        binding.btnTime.setOnClickListener {
            binding.timePicker.visibility = if (binding.timePicker.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // 2. Listeners (Envoi vers ViewModel)
        binding.datePicker.init(
            binding.datePicker.year, binding.datePicker.month, binding.datePicker.dayOfMonth
        ) { _, year, month, day ->
            if (!isRestoringState) {
                val cal = Calendar.getInstance().apply { set(year, month, day) }
                viewModel.setSelectedDate(cal.timeInMillis)
            }
        }

        binding.timePicker.setOnTimeChangedListener { _, hour, minute ->
            if (!isRestoringState) {
                viewModel.setSelectedTime(hour, minute)
            }
        }

        // 3. Observation (Réception du ViewModel) - Pattern Observer
        viewModel.selectedDateTime.observe(viewLifecycleOwner) { cal ->
            isRestoringState = true // Bloque le déclenchement des listeners

            updateDateTimeDisplay(binding, cal)
            binding.datePicker.updateDate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            binding.timePicker.hour = cal.get(Calendar.HOUR_OF_DAY)
            binding.timePicker.minute = cal.get(Calendar.MINUTE)

            isRestoringState = false // Débloque
        }
    }

    private fun setupBusSection(binding: FragmentSelectionBinding) {
        viewModel.allRoutes.observe(viewLifecycleOwner) { routes ->
            val routesWithPrompt = mutableListOf<BusRoute?>(null).apply { addAll(routes) }

            val adapter = object : ArrayAdapter<BusRoute?>(requireContext(), R.layout.row_bus, routesWithPrompt) {
                override fun getView(pos: Int, conv: View?, parent: ViewGroup): View {
                    val item = getItem(pos) ?: return createPromptView()
                    val v = conv ?: LayoutInflater.from(context).inflate(R.layout.row_bus, parent, false)
                    v.findViewById<TextView>(R.id.bus_badge).apply {
                        text = item.route_short_name
                        setBackgroundColor("#${item.route_color}".toColorInt())
                        setTextColor("#${item.route_text_color}".toColorInt())
                    }
                    v.findViewById<TextView>(R.id.bus_name).text = item.route_long_name
                    return v
                }

                private fun createPromptView(): View {
                    return TextView(context).apply {
                        text = "Choisir une ligne..."
                        setPadding(15, 15, 15, 15)
                    }
                }

                override fun getDropDownView(pos: Int, conv: View?, parent: ViewGroup): View {
                    return if (pos == 0) View(context).apply {
                        layoutParams = AbsListView.LayoutParams(0, 0)
                        visibility = View.GONE
                    } else getView(pos, null, parent)
                }
            }

            binding.spinnerBus.adapter = adapter

            // Restauration de la sélection
            viewModel.selectedRoute.value?.let { savedRoute ->
                val position = routes.indexOfFirst { it.route_id == savedRoute.route_id }
                if (position >= 0) binding.spinnerBus.setSelection(position + 1)
            }
        }

        binding.spinnerBus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                val route = binding.spinnerBus.selectedItem as? BusRoute
                viewModel.setSelectedRoute(route)

                if (route != null) {
                    viewModel.loadDirections(route.route_id)
                    binding.listDirections.visibility = View.VISIBLE
                } else {
                    binding.listDirections.visibility = View.GONE
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        viewModel.directions.observe(viewLifecycleOwner) { dirs ->
            binding.listDirections.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                dirs.map { it.trip_headsign }
            )
            binding.listDirections.setOnItemClickListener { _, _, i, _ ->
                val route = binding.spinnerBus.selectedItem as? BusRoute
                route?.let {
                    val action = SelectionFragmentDirections.toStops(it.route_id, dirs[i].direction_id)
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun updateDateTimeDisplay(binding: FragmentSelectionBinding, cal: Calendar) {
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.btnDate.text = "Date : ${sdfDate.format(cal.time)}"
        binding.btnTime.text = "Heure : ${sdfTime.format(cal.time)}"
    }
}