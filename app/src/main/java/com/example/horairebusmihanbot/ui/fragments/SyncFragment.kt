package com.example.horairebusmihanbot.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.databinding.FragmentSyncBinding
import com.example.horairebusmihanbot.repository.SyncRepository
import com.example.horairebusmihanbot.state.SyncState
import com.example.horairebusmihanbot.viewmodel.SyncViewModel
import kotlinx.coroutines.launch

class SyncFragment : Fragment(R.layout.fragment_sync) {
    private val viewModel: SyncViewModel by viewModels()
    private var _binding: FragmentSyncBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSyncBinding.bind(view)
        // On délègue la décision au ViewModel (SOLID)
        viewModel.checkAndStartSync(requireContext())

        // Configuration de la ProgressBar en mode déterminé
        binding.progressBar.isIndeterminate = false
        binding.progressBar.max = 100

        // Dans SyncFragment.kt
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is SyncState.Progress -> {
                            binding.progressBar.isIndeterminate = false
                            binding.progressBar.progress = state.percent
                            binding.textPercent.text = "${state.percent}% - ${state.message}"
                        }
                        is SyncState.Finished -> {
                            findNavController().navigate(SyncFragmentDirections.Companion.toSelection())
                            SyncRepository.update(SyncState.Idle) // On remet à zéro pour après
                        }
                        is SyncState.Error -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        }
                        SyncState.Idle -> { /* Rien à faire */ }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}