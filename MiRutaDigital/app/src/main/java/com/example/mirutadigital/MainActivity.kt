package com.example.mirutadigital

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.mirutadigital.navigation.AppNavigation // Importa el NavHost
import com.example.mirutadigital.ui.screens.MainScreen
import com.example.mirutadigital.ui.theme.MiRutaDigitalTheme // Importa tema
import com.example.mirutadigital.viewModel.LocationViewModel

class MainActivity : ComponentActivity() {

    private val locationViewModel: LocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registrar el launcher para solicitar permisos de ubicación
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Si el permiso es otorgado, iniciar actualizaciones de ubicación
                locationViewModel.startLocationUpdates()
            } else {
                // Si el permiso es denegado, registrar el error
                // La app puede funcionar de forma limitada sin permisos de ubicación
                android.util.Log.w("MainActivity", "Permiso de ubicación denegado")
            }
        }

        setContent {
            MiRutaDigitalTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Solicitar permisos de ubicación al inicio de la app
                    LaunchedEffect(Unit) {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    MainScreen(locationViewModel)
                }
            }
        }
    }
}
