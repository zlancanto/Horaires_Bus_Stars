package com.example.horairebusmihanbot.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.databinding.FragmentStopsBinding
import com.example.horairebusmihanbot.viewmodel.BusViewModel

class StopsFragment : Fragment(R.layout.fragment_stops) {
    private val viewModel: BusViewModel by activityViewModels()
    private val args: StopsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentStopsBinding.bind(view)

        viewModel.loadStops(args.routeId, args.dirId)

        viewModel.stops.observe(viewLifecycleOwner) { stops ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, stops.map { it.stop_name })
            binding.listStops.adapter = adapter

            // Recherche
            binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(q: String?) = false
                override fun onQueryTextChange(q: String?): Boolean {
                    adapter.filter.filter(q)
                    return true
                }
            })

            binding.listStops.setOnItemClickListener { _, _, i, _ ->
                val stop = stops[i]
                val action = StopsFragmentDirections.toSchedule(stop.stop_id)
                findNavController().navigate(action)
            }
        }
    }
}