package com.example.mirutadigital.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mirutadigital.navigation.AppNavigation
import com.example.mirutadigital.navigation.Routes
import com.example.mirutadigital.navigation.navigationItems
import com.example.mirutadigital.ui.components.BottomBar
import com.example.mirutadigital.ui.components.Toolbar
import com.example.mirutadigital.ui.components.content.MainContent
import com.example.mirutadigital.ui.components.content.MapContent
import com.example.mirutadigital.ui.screens.share.ShareViewModel
import com.example.mirutadigital.ui.util.ShareManager
import com.example.mirutadigital.ui.util.SnackbarManager
import com.example.mirutadigital.viewModel.LocationViewModel
import com.example.mirutadigital.viewModel.MapStateViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    locationViewModel: LocationViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val scaffoldState = rememberBottomSheetScaffoldState()
    val isSheetExpanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded
    val scope = rememberCoroutineScope()
    val expandSheet: () -> Unit = {
        scope.launch {
            scaffoldState.bottomSheetState.expand()
        }
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val currentScreen = when {
        currentRoute == Routes.STOPS -> Routes.STOPS
        currentRoute?.contains(Routes.ALL_ROUTES) == true -> Routes.ALL_ROUTES
        currentRoute == Routes.DESTINATION_SEARCH -> Routes.DESTINATION_SEARCH
        currentRoute?.contains(Routes.ROUTE_DETAIL) == true -> Routes.ROUTE_DETAIL
        currentRoute == Routes.FAVORITES -> Routes.FAVORITES
        currentRoute == Routes.HISTORY -> Routes.HISTORY
        currentRoute == Routes.SHARE -> Routes.SHARE
        currentRoute?.contains(Routes.STREET_VIEW) == true -> Routes.STREET_VIEW
        else -> Routes.STOPS
    }

    val selectedIndex = when (currentScreen) {
        Routes.STOPS -> 0
        Routes.ALL_ROUTES -> 1
        Routes.DESTINATION_SEARCH -> 2
        Routes.ROUTE_DETAIL -> 1
        Routes.SHARE -> 3
        else -> 0
    }

    val mapStateViewModel: MapStateViewModel = viewModel()
    val mapState by mapStateViewModel.mapState.collectAsState()

    val shareViewModel: ShareViewModel = viewModel()
    val shareState by shareViewModel.uiState.collectAsState()

    val screenTitle = when (currentScreen) {
        Routes.STOPS -> "Mi Ruta Digital"
        Routes.ALL_ROUTES -> "Ver Rutas"
        Routes.DESTINATION_SEARCH -> "Buscar Destino"
        Routes.ROUTE_DETAIL -> mapState.currentRouteName ?: "Detalle de la Ruta"
        Routes.FAVORITES -> "Gestionar Favoritos"
        Routes.HISTORY -> "Historial de Rutas"
        Routes.SHARE -> "Compartir Ruta"
        Routes.STREET_VIEW -> "Vista Calle"
        else -> ""
    }

    val showBottomBarAndSheet = currentScreen !in listOf(
        Routes.FAVORITES,
        Routes.HISTORY,
        Routes.STREET_VIEW
    )

    val canNavigateBack = currentScreen in listOf(
        Routes.ROUTE_DETAIL,
        Routes.FAVORITES,
        Routes.HISTORY,
        Routes.STREET_VIEW
    )

    val showToolbarMenu = currentScreen !in listOf(
        Routes.FAVORITES,
        Routes.HISTORY,
        Routes.STREET_VIEW
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        SnackbarManager.messages.collectLatest {
            snackbarHostState.showSnackbar(it)
        }
    }

    val handleNavigateBack: () -> Unit = {
        when (currentScreen) {
            Routes.ROUTE_DETAIL -> {
                navController.navigate(Routes.ALL_ROUTES) {
                    popUpTo(Routes.ALL_ROUTES) { inclusive = false }
                    launchSingleTop = true
                }
            }

            else -> navController.navigateUp()
        }
    }

    LaunchedEffect(currentScreen, navBackStackEntry) {
        when (currentScreen) {
            Routes.STOPS -> mapStateViewModel.showAllStops()
            Routes.ALL_ROUTES -> {}
            Routes.DESTINATION_SEARCH -> {}
            Routes.ROUTE_DETAIL -> {
                val routeId = navBackStackEntry?.arguments?.getString("routeId")
                if (routeId != null) {
                    mapStateViewModel.showRouteDetailById(routeId)
                }
            }

            Routes.FAVORITES,
            Routes.HISTORY,
            Routes.SHARE -> {
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            Toolbar(
                title = screenTitle,
                canNavigateBack = canNavigateBack,
                showMenu = showToolbarMenu,
                onNavigateUp = handleNavigateBack,
                onNavigateToFavorites = {
                    navController.navigate(Routes.FAVORITES) {
                        launchSingleTop = true
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.HISTORY) {
                        launchSingleTop = true
                    }
                },
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme
            )
        },
        bottomBar = {
            if (showBottomBarAndSheet) {
                BottomBar(
                    items = navigationItems,
                    selectedIndex = selectedIndex,
                    onItemSelected = { index ->
                        val selectedItem = navigationItems[index]
                        val selectedRoute = selectedItem.route

                        if (selectedRoute != currentScreen) {
                            when (selectedRoute) {
                                Routes.STOPS -> {
                                    navController.navigate(Routes.STOPS) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }

                                Routes.ALL_ROUTES -> {
                                    navController.navigate(Routes.ALL_ROUTES) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }

                                Routes.DESTINATION_SEARCH -> {
                                    navController.navigate(Routes.DESTINATION_SEARCH) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }

                                Routes.SHARE -> {
                                    navController.navigate(Routes.SHARE) {
                                        launchSingleTop = true
                                        restoreState = false // no se
                                    }
                                }
                            }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (showBottomBarAndSheet) {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                modifier = Modifier
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    },
                sheetContent = {
                    Box(modifier = Modifier.fillMaxHeight(0.5f)) {
                        AppNavigation(
                            navController = navController,
                            locationViewModel = locationViewModel,
                            mapStateViewModel = mapStateViewModel,
                            isSheetExpanded = isSheetExpanded,
                            onExpandSheet = expandSheet
                        )
                    }
                },
                sheetPeekHeight = 93.dp,
                sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                sheetDragHandle = {
                    Box(modifier = Modifier.padding(6.dp)) {
                        Box(
                            modifier = Modifier
                                .width(105.dp)
                                .height(9.dp)
                                .padding(top = 4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }
                }
            ) { innerPaddingSheet ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    MainContent(padding = innerPaddingSheet, inProduction = false) {
                        MapContent(mapStateViewModel = mapStateViewModel)
                    }

                    val currentShared by ShareManager.currentSharedRouteId.collectAsState()
                    val routeName = shareState.routes.find { it.id == currentShared }?.name
                    val routeLabel = shareState.routes.find { it.id == currentShared }?.windshieldLabel ?: routeName
                    var showCancelDialog by remember { mutableStateOf(false) }

                    if (currentShared != null && !listOf(
                            Routes.FAVORITES,
                            Routes.HISTORY
                        ).contains(currentRoute)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .align(Alignment.TopCenter),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Compartiendo ruta ${routeName ?: "ruta"}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface // CORREGIDO: Color válido
                            )
                            Button(
                                onClick = { showCancelDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    contentColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dejar de compartir"
                                    )
                                    Text("Dejar de compartir")
                                }
                            }
                        }
                    }

                    if (showCancelDialog) {
                        AlertDialog(
                            onDismissRequest = { showCancelDialog = false },
                            title = { Text("Dejar de Compartir") },
                            text = { Text("¿Seguro que quieres dejar de compartir?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showCancelDialog = false
                                    shareViewModel.stopShare()
                                }) { Text("Sí") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showCancelDialog = false
                                }) { Text("No") }
                            }
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    }
            ) {
                AppNavigation(
                    navController = navController,
                    locationViewModel = locationViewModel,
                    mapStateViewModel = mapStateViewModel,
                    isSheetExpanded = true,
                    onExpandSheet = {}
                )
            }
        }
    }
}