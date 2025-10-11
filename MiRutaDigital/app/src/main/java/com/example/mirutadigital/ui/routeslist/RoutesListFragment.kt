package com.example.mirutadigital.ui.routeslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mirutadigital.databinding.FragmentRoutesListBinding
import com.example.mirutadigital.ui.viewmodel.RoutesListViewModel
import com.example.mirutadigital.ui.viewmodel.ViewModelFactory
import com.example.mirutadigital.App

class RoutesListFragment : Fragment() {

    private var _binding: FragmentRoutesListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RoutesListViewModel by viewModels {
        ViewModelFactory((requireActivity().application as App).repository)
    }
    private lateinit var routesAdapter: RoutesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        routesAdapter = RoutesAdapter()
        binding.recyclerViewRoutes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = routesAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.availableRoutes.observe(viewLifecycleOwner, Observer { routes ->
            routesAdapter.submitList(routes)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
