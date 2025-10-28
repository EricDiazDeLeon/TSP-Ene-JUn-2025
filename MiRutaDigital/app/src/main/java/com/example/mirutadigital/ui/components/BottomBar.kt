package com.example.mirutadigital.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mirutadigital.navigation.Routes


// se modifica NavItem para usar la ruta de navegación
data class NavItem(val label: String, val icon: ImageVector, val route: String)

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
            val isShareButton = item.route == Routes.SHARE

            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = {
                    // dejar solo onItemSelected(index) cuando se implemente el comparitir
                    if (!isShareButton) {
                        onItemSelected(index)
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        color = MaterialTheme.colorScheme.onSecondaryFixedVariant
                    )
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        tint = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                        contentDescription = item.label
                    )
                },
                alwaysShowLabel = true,
                // solo si es el boton compartir se desabilita
                enabled = !isShareButton,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            )
        }
    }
}
