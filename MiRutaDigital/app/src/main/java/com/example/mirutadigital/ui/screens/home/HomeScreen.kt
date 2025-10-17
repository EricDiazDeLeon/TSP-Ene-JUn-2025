package com.example.mirutadigital.ui.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mirutadigital.data.pruebasUi.StopWithRoutes
import com.example.mirutadigital.navigation.navigationItems
import com.example.mirutadigital.ui.components.BottomBar
import com.example.mirutadigital.ui.components.MainContent
import com.example.mirutadigital.ui.components.MapContent
import com.example.mirutadigital.ui.components.Toolbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import com.google.maps.android.BuildConfig
import androidx.compose.ui.platform.LocalContext
import com.example.mirutadigital.R


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeScreenViewModel = viewModel()) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // observa el estado completo de la ui desde el vm
    val uiState by viewModel.uiState.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var showRoutesSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var expandedStopId by remember { mutableStateOf<String?>(null) }
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val lazyListState = rememberLazyListState()

    val context = LocalContext.current


    // Si el usuario abre el sheet, mostramos el ModalBottomSheet
    if (showRoutesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRoutesSheet = false },
            sheetState = sheetState
        ) {
            // Contenido del BottomSheet (la lista de rutas)
            RoutesBottomSheetContent(
                routes = uiState.routes,
                onRouteClick = { route ->

                    var apiKey = context.getString(R.string.maps_api_key)
                    viewModel.onRouteSelected(route, context.getString(R.string.maps_api_key))

                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showRoutesSheet = false
                        }
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = { Toolbar() },
        bottomBar = {
            BottomBar(
                items = navigationItems,
                selectedIndex = 0, // El índice siempre será 0 (Inicio)
                onItemSelected = { index ->
                    // 2. CAMBIAMOS LA LÓGICA DEL BOTÓN "VER RUTAS"
                    val selectedItem = navigationItems[index]
                    if (selectedItem.route == "routes_screen") {
                        // En lugar de navegar, ahora mostramos el BottomSheet
                        showRoutesSheet = true
                    } else if (selectedItem.route != "share_action") {
                        // La navegación a otras pantallas (si las hubiera) sigue igual
                        navController.navigate(selectedItem.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        BottomSheetScaffold(
            modifier = Modifier
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                },
            content = { innerPaddingSheet ->
                MainContent(padding = innerPaddingSheet) {
                    // 3. PASAMOS LA RUTA SELECCIONADA AL MAPA
                    MapContent(
                        padding = innerPaddingSheet,
                        detailedPolyline = uiState.detailedPolyline
                    )
                }
            },
            sheetContent = {
                Box(modifier = Modifier.fillMaxHeight(0.5f)) {
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
                                searchQuery = uiState.searchQuery, // <-- Consume el estado
                                onSearchQueryChange = viewModel::onSearchQueryChange // <-- Dispara el evento
                            )
                        }
                        // la lista se construye con los datos ya filtrados desde el vm
                        items(uiState.filteredStops, key = { it.id }) { stop ->
                            StopItem(
                                stop = stop,
                                isExpanded = stop.id == expandedStopId,
                                navController = navController,
                                onItemClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    val previouslyExpanded = expandedStopId == stop.id
                                    expandedStopId = if (previouslyExpanded) null else stop.id
                                    if (!previouslyExpanded) {
                                        scope.launch {
                                            val index = uiState.filteredStops.indexOfFirst { it.id == stop.id }
                                            if (index != -1) {
                                                delay(100)
                                                lazyListState.animateScrollToItem(index)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            },
            sheetPeekHeight = 115.dp,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetDragHandle = {
                BottomSheetDefaults.DragHandle(
                    width = 90.dp,
                    color = Color.Gray
                )
            }
        )
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
    stop: StopUi,
    isExpanded: Boolean,
    navController: NavController,
    onItemClick: () -> Unit
) {
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
                        "${stop.latitude}, ${stop.longitude}",
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
                    stop.routes.forEach { route ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${route.name} (${route.operatingHours})")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        enabled = false,
                        onClick = { navController.navigate("routes_screen") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Ver Rutas")
                    }
                } else {
                    Text("No hay rutas que pasen por esta parada.")
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
    HomeScreen(navController = navController, viewModel = viewModel())
}

@Composable
fun RoutesBottomSheetContent(
    routes: List<com.example.mirutadigital.data.model.Route>,
    onRouteClick: (com.example.mirutadigital.data.model.Route) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            "Rutas disponibles",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(routes) { route ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onRouteClick(route) },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(route.name, style = MaterialTheme.typography.titleMedium)
                        Text("Horario: ${route.operatingHours}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp)) // Espacio al final
            }
        }
    }
}
