package com.example.horairebusmihanbot.ui

import android.graphics.Color
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

class SelectionFragment : Fragment(R.layout.fragment_selection) {
    private val viewModel: BusViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentSelectionBinding.bind(view)

        // 1. Remplir le Spinner des lignes de bus
        viewModel.allRoutes.observe(viewLifecycleOwner) { routes ->
            val adapter = object : ArrayAdapter<BusRoute>(requireContext(), R.layout.row_bus, routes) {
                override fun getView(pos: Int, conv: View?, parent: ViewGroup): View {
                    val v = conv ?: LayoutInflater.from(context).inflate(R.layout.row_bus, parent, false)
                    val item = getItem(pos)!!
                    v.findViewById<TextView>(R.id.bus_badge).apply {
                        text = item.route_short_name
                        setBackgroundColor(Color.parseColor("#${item.route_color}"))
                        setTextColor(Color.parseColor("#${item.route_text_color}"))
                    }
                    v.findViewById<TextView>(R.id.bus_name).text = item.route_long_name
                    return v
                }
                override fun getDropDownView(pos: Int, conv: View?, parent: ViewGroup) = getView(pos, conv, parent)
            }
            binding.spinnerBus.adapter = adapter
        }

        // 2. Quand on choisit une ligne, charger les directions
        binding.spinnerBus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                val route = binding.spinnerBus.selectedItem as BusRoute
                viewModel.loadDirections(route.route_id)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // 3. Afficher les directions dans la liste
        viewModel.directions.observe(viewLifecycleOwner) { dirs ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dirs.map { it.trip_headsign })
            binding.listDirections.adapter = adapter

            binding.listDirections.setOnItemClickListener { _, _, i, _ ->
                val route = binding.spinnerBus.selectedItem as BusRoute
                val dir = dirs[i]
                // Navigation vers Fragment 2 avec arguments
                val action = SelectionFragmentDirections.toStops(route.route_id, dir.direction_id)
                findNavController().navigate(action)
            }
        }
    }
}