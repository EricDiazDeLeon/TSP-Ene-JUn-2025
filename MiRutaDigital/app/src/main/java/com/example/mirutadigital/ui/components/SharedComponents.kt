package com.example.mirutadigital.ui.components

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import com.google.maps.android.compose.Polyline
import com.example.mirutadigital.data.model.Route
import com.google.android.gms.maps.CameraUpdateFactory

// se modifica NavItem para usar la ruta de navegación
data class NavItem(val label: String, val icon: ImageVector, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Mi Ruta Digital",
                fontWeight = Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.secondary
        )
    )
}

// componentes compartidos
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        items.forEachIndexed { index, item ->
            // por ahora para que no se precione este boton hasta implementarlo
            val isShareButton = item.route == "share_action"

            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = {
                    // dejar solo onItemSelected(index) cuando se implemente el comparitir
                    if (!isShareButton) {
                        onItemSelected(index)
                    }
                },
                label = { Text(text = item.label) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                alwaysShowLabel = true ,
                // solo si es el boton compartir se desabilita
                enabled = !isShareButton
            )
        }
    }
}

@Composable
fun MainContent(
    padding: PaddingValues,
    content: @Composable () -> Unit // para recibir el contenido
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// composable del mapa
@Composable
fun MapContent(
    padding: PaddingValues,
    selectedRoute: Route? = null, // <-- AÑADE ESTE PARÁMETRO
    detailedPolyline: List<LatLng> = emptyList()
) {
    val pos_coordenadas = LatLng(22.762687558171876, -102.58071323639645)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pos_coordenadas, 14f)
    }

    // (Opcional) Si se selecciona una ruta, puedes animar la cámara a esa ruta
    LaunchedEffect(detailedPolyline) {
        if (detailedPolyline.isNotEmpty()) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(detailedPolyline.first(), 14f),
                durationMs = 1000
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // DIBUJA LA POLILÍNEA SI HAY UNA RUTA SELECCIONADA
        if (detailedPolyline.isNotEmpty()) {
            Polyline(points = detailedPolyline)
        }
    }
}