package com.example.horairebusmihanbot.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.databinding.FragmentDetailsBinding

class DetailsFragment : Fragment(R.layout.fragment_details) {
    private val args: DetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentDetailsBinding.bind(view)
        binding.detailName.text = "Arrêt : ${args.stopId}"
        binding.detailCoords.text = "Latitude: 48.11 \nLongitude: -1.67"

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}