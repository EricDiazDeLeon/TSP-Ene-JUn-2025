package com.example.mirutadigital

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mirutadigital.navigation.AppNavigation // Importa el NavHost
import com.example.mirutadigital.ui.theme.MiRutaDigitalTheme // Importa tema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiRutaDigitalTheme(dynamicColor = false) { // Usamos el tema y desactivamos el color dinámico
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation() // el NavHost es ahora el punto de entrada de la ui
                }
            }
        }
    }
}
