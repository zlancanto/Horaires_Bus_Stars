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
        if (viewModel.state.value is SyncState.Idle) {
            viewModel.checkAndStartSync(requireContext())
        }

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
                            findNavController().navigate(SyncFragmentDirections.toSelection())
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

    /*private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. Observer l'état de l'interface (UI State)
                launch {
                    viewModel.state.collect { state ->
                        updateUI(state)
                    }
                }

                // 2. Observer l'événement de navigation (Single Event Pattern)
                // SharedFlow garantit que l'événement n'est consommé qu'une fois
                launch {
                    viewModel.navigationEvent.collect {
                        navigateToSelection()
                    }
                }
            }
        }
    }

    private fun updateUI(state: SyncState) {
        when (state) {
            is SyncState.Progress -> {
                binding.progressBar.isIndeterminate = false
                binding.progressBar.progress = state.percent
                binding.textPercent.text = "${state.percent}% - ${state.message}"
            }
            is SyncState.Finished -> {
                navigateToSelection()
            }
            is SyncState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            SyncState.Idle -> { /* On ne fait rien */ }
        }
    }

    private fun navigateToSelection() {
        // On vérifie si on est toujours sur ce fragment avant de naviguer
        if (findNavController().currentDestination?.id == R.id.fragment_sync) {
            findNavController().navigate(SyncFragmentDirections.toSelection())
            SyncRepository.update(SyncState.Idle)
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}