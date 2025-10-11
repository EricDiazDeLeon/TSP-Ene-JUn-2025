package com.example.mirutadigital.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.mirutadigital.R
import com.example.mirutadigital.databinding.FragmentMapBinding
import com.example.mirutadigital.ui.viewmodel.MapViewModel
import com.example.mirutadigital.ui.viewmodel.ViewModelFactory
import com.example.mirutadigital.App
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private val viewModel: MapViewModel by viewModels {
        ViewModelFactory((requireActivity().application as App).repository)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        setupMap()
        observeViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setupUI()
    }

    private fun setupUI() {
        // Configurar botón de Paradas Cercanas
        binding.btnNearbyStops.setOnClickListener {
            // TODO: Implementar funcionalidad de paradas cercanas
        }

        // Configurar navegación inferior
        binding.navHome.setOnClickListener {
            // Ya estamos en la pantalla de inicio
        }

        binding.navRoutes.setOnClickListener {
            viewModel.onShowRoutesListClicked()
        }

        binding.navShare.setOnClickListener {
            viewModel.onShareLocationClicked()
        }

        // Configurar barra de búsqueda
        binding.searchEditText.setOnEditorActionListener { _, _, _ ->
            // TODO: Implementar búsqueda
            true
        }
    }

    private fun setupMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar permisos
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Centrar en una ubicación por defecto (ejemplo: Ciudad de México)
        val defaultLocation = LatLng(19.4326, -99.1332)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
    }

    private fun observeViewModel() {
        viewModel.routes.observe(viewLifecycleOwner, Observer { routes ->
            drawRoutesOnMap(routes)
        })

        viewModel.stops.observe(viewLifecycleOwner, Observer { stops ->
            updateStopMarkers(stops)
        })

        viewModel.liveTrucks.observe(viewLifecycleOwner, Observer { trucks ->
            updateTruckMarkers(trucks)
        })

        viewModel.navigateToRoutesList.observe(viewLifecycleOwner, Observer { shouldNavigate ->
            if (shouldNavigate) {
                // Navegar a la lista de rutas
                viewModel.onNavigationHandled()
            }
        })

        viewModel.navigateToSharing.observe(viewLifecycleOwner, Observer { shouldNavigate ->
            if (shouldNavigate) {
                // Navegar a la pantalla de compartir
                viewModel.onNavigationHandled()
            }
        })
    }

    private fun drawRoutesOnMap(routes: List<com.example.mirutadigital.data.model.Route>) {
        googleMap.clear()
        
        routes.forEach { route ->
            if (route.polylinePoints.isNotEmpty()) {
                val polylineOptions = PolylineOptions()
                    .addAll(route.polylinePoints)
                    .color(android.graphics.Color.BLUE)
                    .width(5f)
                
                googleMap.addPolyline(polylineOptions)
            }
        }
    }

    private fun updateStopMarkers(stops: List<com.example.mirutadigital.data.model.Stop>) {
        stops.forEach { stop ->
            googleMap.addMarker(
                MarkerOptions()
                    .position(stop.location)
                    .title(stop.name)
            )
        }
    }

    private fun updateTruckMarkers(trucks: List<com.example.mirutadigital.data.model.LiveTruck>) {
        trucks.forEach { truck ->
            googleMap.addMarker(
                MarkerOptions()
                    .position(truck.location)
                    .title("Camión ${truck.truckId}")
                    .snippet("Observadores: ${truck.viewersCount}")
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
