package com.example.mirutadigital.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar(
    title: String,
    canNavigateBack: Boolean,
    showMenu: Boolean = true,
    onNavigateUp: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: (Boolean) -> Unit = {}
) {
    var showMenuDropdown by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        imageVector = Icons.Default.ArrowBackIosNew,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Atras"
                    )
                }
            }
        },
        actions = {
            if (showMenu) {
                IconButton(onClick = { showMenuDropdown = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menú",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = showMenuDropdown,
                    onDismissRequest = { showMenuDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Modo oscuro") },
                        onClick = { onToggleDarkTheme(!isDarkTheme); showMenuDropdown = false },
                        trailingIcon = {
                            Switch(checked = isDarkTheme, onCheckedChange = {
                                onToggleDarkTheme(it); showMenuDropdown = false
                            })
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Gestionar Favoritos") },
                        onClick = {
                            showMenuDropdown = false
                            onNavigateToFavorites()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favoritos"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Historial de Rutas") },
                        onClick = {
                            showMenuDropdown = false
                            onNavigateToHistory()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Historial"
                            )
                        }
                    )
                }
            }
        }
    )
}
