package com.example.mirutadigital

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.mirutadigital.navigation.AppNavigation // Importa el NavHost
import com.example.mirutadigital.ui.theme.MiRutaDigitalTheme // Importa tema
import androidx.activity.compose.rememberLauncherForActivityResult

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiRutaDigitalTheme(dynamicColor = false) { // Usamos el tema y desactivamos el color dinámico
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RequestLocationPermissionOnLaunch()
                    AppNavigation() // el NavHost es ahora el punto de entrada de la ui
                }
            }
        }
    }
}

@Composable
private fun RequestLocationPermissionOnLaunch() {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { /* no-op; VMs will re-check permission */ }
    )

    // Solicitar permisos al abrir la app
    LaunchedEffect(Unit) {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
