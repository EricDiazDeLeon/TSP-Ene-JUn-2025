package com.example.mirutadigital.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// colores
private val LightBlue = Color(0xFF00BCD4) // parecido a HUE_AZURE
private val White = Color(0xFFFFFFFF)
private val Red = Color(0xFFFF0000)


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