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

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean ->
            if (isGranted) {
                locationViewModel.startLocationUpdates()
            }
        }

        setContent {
            MiRutaDigitalTheme(dynamicColor = false) { // Usamos el tema y desactivamos el color dinámico
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LaunchedEffect(Unit) {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    MainScreen(locationViewModel) // el NavHost es ahora el punto de entrada de la ui
                }
            }
        }
    }
}
