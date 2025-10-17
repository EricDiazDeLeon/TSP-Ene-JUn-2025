package com.example.mirutadigital.ui.screens.home

import android.location.Location
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.mirutadigital.viewModel.LocationViewModel
import com.example.mirutadigital.data.testsUi.StopWithRoutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StopsSheetContent(
    viewModel: StopsViewModel = viewModel(),
    locationViewModel: LocationViewModel
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // observa el estado completo de la ui desde el vm
    val uiState by viewModel.uiState.collectAsState()
    val userLocation by locationViewModel.location.collectAsState()

    var expandedStopId by remember { mutableStateOf<String?>(null) }
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()


    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        stickyHeader {
            // obtiene el estado y los eventos del vm
            SheetHeader(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow),
                searchQuery = uiState.searchQuery, // <-- consume el estado
                onSearchQueryChange = viewModel::onSearchQueryChange // <-- dispara el evento
            )
        }
        // la lista se construye con los datos ya filtrados desde el vm
        items(uiState.filteredStops, key = { it.id }) { stop ->
            StopItem(
                stop = stop,
                isExpanded = stop.id == expandedStopId,
                onItemClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    val previouslyExpanded = expandedStopId == stop.id
                    expandedStopId = if (previouslyExpanded) null else stop.id
                    if (!previouslyExpanded) {
                        scope.launch {
                            val index =
                                uiState.filteredStops.indexOfFirst { it.id == stop.id }
                            if (index != -1) {
                                delay(100)
                                lazyListState.animateScrollToItem(index)
                            }
                        }
                    }
                },
                userLocation = userLocation
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}


@Composable
fun SheetHeader(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSecondaryFixedVariant
                )
            ) {
                Icon(Icons.Default.FrontHand, contentDescription = "Paradas Cercanas")
                Spacer(modifier = Modifier.height(40.dp))
                Text("Paradas Cercanas")
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
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun StopItem(
    stop: StopWithRoutes,
    isExpanded: Boolean,
    onItemClick: () -> Unit,
    userLocation: Location?
) {
    var distanceText by remember { mutableStateOf("Calculando...") }
    // controla que cambie si la userLocation cambia
    LaunchedEffect(userLocation) {
        distanceText = userLocation?.let { loc ->
            val stopLocation = Location("").apply {
                latitude = stop.latitude
                longitude = stop.longitude
            }
            val distanceInMeters = loc.distanceTo(stopLocation)

            when {
                distanceInMeters < 1000 -> {
                    "Desde tu ubicación a $distanceInMeters metros"
                }

                else -> {
                    val distanceInKm = distanceInMeters / 1000.0
                    "Desde tu ubicación a %.1f km".format(distanceInKm)
                }
            }
        } ?: "No hay ubicación"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onItemClick)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stop.name, fontWeight = FontWeight.Bold)
                    Text(
                        distanceText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir/Colapsar"
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                if (stop.routes.isNotEmpty()) {
                    val maxRoutesToShow = 3 // cauntas rutas se muestran
                    val routesToShow =
                        stop.routes.take(maxRoutesToShow) // toma solo las primeras n rutas

                    routesToShow.forEach { route ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = route.name,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(0.2f)
                            )
                            Text(
                                text = route.destination,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    }

                    // menasje indicador de que hay mas rutas
                    if (stop.routes.size > maxRoutesToShow) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "...y ${stop.routes.size - maxRoutesToShow} rutas más.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Start) // Alinea a la izquierda
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        enabled = false,
                        onClick = { "TODO: Ver Rutas"},
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Ver Rutas")
                    }
                } else {
                    Text("No hay rutas en esta parada")
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    // com ono puede obtener la ubicacion real no se puede previsualizar
    //HomeScreen(navController = navController, viewModel = viewModel())
}
