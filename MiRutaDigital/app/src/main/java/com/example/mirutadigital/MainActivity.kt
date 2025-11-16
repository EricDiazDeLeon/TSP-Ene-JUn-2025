package com.example.mirutadigital

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.mirutadigital.ui.components.permissions.AwaitingPermissionScreen
import com.example.mirutadigital.ui.components.permissions.PermissionDeniedScreen
import com.example.mirutadigital.ui.screens.MainScreen
import com.example.mirutadigital.ui.theme.MiRutaDigitalTheme
import com.example.mirutadigital.viewModel.LocationViewModel


class MainActivity : ComponentActivity() {

    private val locationViewModel: LocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val app = LocalContext.current.applicationContext as MiRutaApplication
            var isDarkTheme by remember { mutableStateOf(app.userPreferences.getDarkModeEnabled()) }
            MiRutaDigitalTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HandleLocationPermission(
                        locationViewModel = locationViewModel,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { enabled ->
                            isDarkTheme = enabled
                            app.userPreferences.setDarkModeEnabled(enabled)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HandleLocationPermission(
    locationViewModel: LocationViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequestTriggered by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            hasLocationPermission = isGranted
            if (isGranted) {
                locationViewModel.startLocationUpdates()
            }
            permissionRequestTriggered = true
        }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    when {
        hasLocationPermission -> {
            MainScreen(
                locationViewModel = locationViewModel,
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme
            )
        }
        permissionRequestTriggered -> {
            PermissionDeniedScreen {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
        else -> {
            AwaitingPermissionScreen()
        }
    }
}
