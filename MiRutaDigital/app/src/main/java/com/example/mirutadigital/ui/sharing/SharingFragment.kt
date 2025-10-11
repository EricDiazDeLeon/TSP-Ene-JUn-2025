package com.example.mirutadigital.ui.sharing

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.mirutadigital.databinding.FragmentSharingBinding
import com.example.mirutadigital.service.LocationSharingService
import com.example.mirutadigital.ui.viewmodel.SharingViewModel
import com.example.mirutadigital.ui.viewmodel.ViewModelFactory
import com.example.mirutadigital.App

class SharingFragment : Fragment() {

    private var _binding: FragmentSharingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharingViewModel by viewModels {
        ViewModelFactory((requireActivity().application as App).repository)
    }
    private var truckId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        truckId = arguments?.getString("truck_id") ?: ""
        
        setupUI()
        observeViewModel()
        
        if (truckId.isNotEmpty()) {
            viewModel.startListening(truckId)
        }
    }

    private fun setupUI() {
        binding.buttonStopSharing.setOnClickListener {
            stopSharing()
        }
    }

    private fun observeViewModel() {
        viewModel.sharingStatus.observe(viewLifecycleOwner, Observer { status ->
            binding.textStatus.text = status
        })

        viewModel.viewersCount.observe(viewLifecycleOwner, Observer { count ->
            binding.textViewersCount.text = "Personas ayudadas: $count"
        })

        viewModel.isSharing.observe(viewLifecycleOwner, Observer { isSharing ->
            binding.buttonStopSharing.isEnabled = isSharing
        })
    }

    private fun stopSharing() {
        val intent = Intent(requireContext(), LocationSharingService::class.java)
        requireContext().stopService(intent)
        viewModel.stopSharing()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
