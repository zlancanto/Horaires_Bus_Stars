package com.example.horairebusmihanbot.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.databinding.FragmentScheduleBinding

class ScheduleFragment : Fragment(R.layout.fragment_schedule) {
    private val args: ScheduleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentScheduleBinding.bind(view)
        binding.stopNameTitle.text = "Passages à l'arrêt : ${args.stopId}"

        // Ici on simule des horaires (normalement on requête le DAO)
        val dummyTimes = listOf("08:12", "08:24", "08:36", "08:48", "09:00")
        binding.listTimes.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dummyTimes)

        binding.listTimes.setOnItemClickListener { _, _, _, _ ->
            val action = ScheduleFragmentDirections.toDetails(args.stopId)
            findNavController().navigate(action)
        }
    }
}