package com.example.horairebusmihanbot.ui

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.databinding.FragmentScheduleBinding
import com.example.horairebusmihanbot.viewmodel.BusViewModel
import java.util.Locale

class ScheduleFragment : Fragment(R.layout.fragment_schedule) {
    private val viewModel: BusViewModel by activityViewModels()
    private val args: ScheduleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentScheduleBinding.bind(view)

        loadingData()
        observeResults(binding)
        goToStopSelectedView(binding)
    }

    private fun loadingData() {
        viewModel.loadNextPassages(
            routeId = args.routeId,
            directionId = args.dirId,
            stopId = args.stopId,
        )
    }

    private fun observeResults(binding: FragmentScheduleBinding) {
        viewModel.stopTimes.observe(viewLifecycleOwner) { passages ->
            if (passages.isNullOrEmpty()) {
                // Gestion de l'état "Aucun bus"
                binding.stopNameTitle.text = getString(R.string.fschedule_no_bus_found)
                binding.listTimes.adapter = null
            }
            else {
                val dateTime = viewModel.selectedDateTime.value ?: Calendar.getInstance()
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.stopDateTime.text = getString(R.string.fschedule_date_time) + " : ${sdf.format(dateTime)}"
                binding.stopNameTitle.text = getString(R.string.fschedule_passages_at_a_standstill) + " : ${args.stopName}"

                // On transforme les objets StopTime en chaînes de caractères lisibles
                val displayTimes = passages.map { it.departure_time }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    displayTimes
                )
                binding.listTimes.adapter = adapter
            }
        }
    }

    private fun goToStopSelectedView(binding: FragmentScheduleBinding) {
        binding.listTimes.setOnItemClickListener { _, _, position, _ ->
            val selectedPassage = viewModel.stopTimes.value?.get(position)
            selectedPassage?.let {
                val action = ScheduleFragmentDirections.toDetails(
                    tripId = it.trip_id,
                    stopSequence = it.stop_sequence
                )
                findNavController().navigate(action)
            }
        }
    }
}