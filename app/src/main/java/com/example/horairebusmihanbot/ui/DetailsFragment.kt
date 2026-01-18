package com.example.horairebusmihanbot.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.databinding.FragmentDetailsBinding
import com.example.horairebusmihanbot.dto.StopTimeWithLabelDto
import com.example.horairebusmihanbot.ui.adapters.TimelineAdapter
import com.example.horairebusmihanbot.viewmodel.BusViewModel

class DetailsFragment : Fragment(R.layout.fragment_details) {
    private val viewModel: BusViewModel by activityViewModels()
    private val args: DetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentDetailsBinding.bind(view)

        val direction: LiveData<StopTimeWithLabelDto?> = viewModel.tripDetails.map {
            list -> list.lastOrNull()
        }

        binding.tripDetailsDirection.text = direction.value.toString()

        val timelineAdapter = TimelineAdapter()
        binding.listTripStops.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = timelineAdapter
        }

        viewModel.loadTripDetails(args.tripId, args.stopSequence)

        viewModel.tripDetails.observe(viewLifecycleOwner) { details ->
            timelineAdapter.submitList(details)
            if (!details.isNullOrEmpty()) {
                val direction = details.last()
                binding.tripDetailsDirection.text = getString(R.string.fstops_sel_direction) + " : ${direction.stop_name}"
            }
        }
    }
}