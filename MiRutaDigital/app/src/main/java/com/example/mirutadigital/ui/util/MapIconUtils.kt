package com.example.mirutadigital.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// colores
private val LightBlue = Color(0xFF00BCD4) // parecido a HUE_AZURE
private val White = Color(0xFFFFFFFF)
private val Red = Color(0xFFFF0000)
private val Green = Color(0xFF4CAF50)


// composables de iconos
// punto blanco con azul
@Composable
fun InactiveStopIcon() {
    Box(
        modifier = Modifier
            .size(16.dp)
            .border(2.dp, LightBlue, CircleShape)
            .clip(CircleShape)
            .background(White)
    )
}

// punto rojo con blanco
@Composable
fun ActiveStopIcon() {
    Box(
        modifier = Modifier
            .size(16.dp)
            .border(2.dp, White, CircleShape)
            .clip(CircleShape)
            .background(Red)
    )
}

// punto verde con blanco para el destino
@Composable
fun DestinationIcon() {
    Box(
        modifier = Modifier
            .size(16.dp)
            .border(2.dp, White, CircleShape)
            .clip(CircleShape)
            .background(Green)
    )
}

// icono bus
@Composable
fun SharedBusIcon(color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, Color.White, CircleShape)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsBus,
            contentDescription = "Vehículo Compartido",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}