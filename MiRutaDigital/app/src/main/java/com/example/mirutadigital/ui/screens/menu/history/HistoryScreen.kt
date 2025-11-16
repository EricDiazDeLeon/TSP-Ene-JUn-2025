package com.example.mirutadigital.ui.screens.menu.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mirutadigital.data.local.entities.RouteHistoryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 4.dp)
    ) {
        stickyHeader {
            HistoryHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                saveHistoryEnabled = uiState.saveHistoryEnabled,
                onToggleSaveHistory = viewModel::toggleSaveHistoryEnabled,
                historyCount = uiState.historyCount,
                onDeleteAll = viewModel::deleteAllHistory
            )
        }

        items(uiState.filteredHistory, key = { it.id }) { historyItem ->
            HistoryItem(
                historyItem = historyItem,
                onDelete = { viewModel.deleteHistoryItem(historyItem.id) }
            )
        }

        item {
            if (uiState.filteredHistory.isEmpty() && !uiState.isLoading) {
                Text(
                    if (uiState.saveHistoryEnabled) {
                        "No hay rutas en el historial..."
                    } else {
                        "El guardado de historial está desactivado"
                    },
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HistoryHeader(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    saveHistoryEnabled: Boolean,
    onToggleSaveHistory: () -> Unit,
    historyCount: Int,
    onDeleteAll: () -> Unit
) {
    Column(modifier = modifier) {
        // boton switch para activar/desactivar el guardado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Guardar historial de rutas compartidas",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
                Text(
                    text = if (saveHistoryEnabled) "Activado" else "Desactivado",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Switch(
                checked = saveHistoryEnabled,
                onCheckedChange = { onToggleSaveHistory() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    checkedTrackColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Buscador
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* TODO */ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSecondaryFixedVariant
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Historial"
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Historial\n($historyCount)",
                    textAlign = TextAlign.Center
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSecondaryFixedVariant
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Borrar texto"
                            )
                        }
                    }
                }
            )
        }

        if (historyCount > 0) {
            Button(
                onClick = onDeleteAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Eliminar todo")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar todo el historial")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryItem(
    historyItem: RouteHistoryEntity,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(historyItem.sharedAt))

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = historyItem.routeName,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
                Text(
                    text = "Compartida el $formattedDate",
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar del historial",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}