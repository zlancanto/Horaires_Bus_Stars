package com.example.horairebusmihanbot.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.databinding.FragmentSyncBinding

class SyncFragment : Fragment(R.layout.fragment_sync) {

    // On écoute quand le remplissage est fini
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            findNavController().navigate(SyncFragmentDirections.toSelection())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSyncBinding.bind(view)

        // On simule une progression visuelle
        binding.progressBar.isIndeterminate = true

        // Enregistrement de l'écouteur
        requireContext().registerReceiver(receiver, IntentFilter("DATA_READY"), Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(receiver)
    }
}