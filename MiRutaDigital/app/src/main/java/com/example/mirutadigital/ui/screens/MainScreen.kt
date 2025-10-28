package com.example.mirutadigital.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mirutadigital.data.local.AppDatabase
import com.example.mirutadigital.data.remote.FirestoreService
import com.example.mirutadigital.data.repository.AppRepository
import com.example.mirutadigital.navigation.AppNavigation
import com.example.mirutadigital.navigation.Routes
import com.example.mirutadigital.navigation.navigationItems
import com.example.mirutadigital.ui.components.BottomBar
import com.example.mirutadigital.ui.components.SharingStatusIndicator
import com.example.mirutadigital.ui.components.Toolbar
import com.example.mirutadigital.ui.components.content.MainContent
import com.example.mirutadigital.ui.components.content.MapContent
import com.example.mirutadigital.ui.screens.shareLocation.ShareLocationScreen
import com.example.mirutadigital.viewModel.LocationViewModel
import com.example.mirutadigital.viewModel.MapStateViewModel
import com.example.mirutadigital.viewModel.MapStateViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(locationViewModel: LocationViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val currentScreen = when {
        currentRoute == Routes.STOPS -> Routes.STOPS
        currentRoute?.contains(Routes.ALL_ROUTES) == true -> Routes.ALL_ROUTES
        currentRoute == Routes.SHARE_LOCATION_SCREEN -> Routes.SHARE_LOCATION_SCREEN
        else -> Routes.STOPS
    }

//    val selectedIndex = navigationItems.indexOfFirst { it.route == currentScreen }
//        .coerceAtLeast(0)

    val selectedIndex = when (currentScreen) {
        Routes.STOPS -> 0
        Routes.ALL_ROUTES -> 1
        Routes.SHARE_LOCATION_SCREEN -> 2
        else -> 0
    }

    val screenTitle = when (currentScreen) {
        Routes.STOPS -> "Mi Ruta Digital"
        Routes.ALL_ROUTES -> "Ver Rutas"
        Routes.SHARE_LOCATION_SCREEN -> "Compartir Ubicación"
        //Routes.ACTIVE_ROUTES -> "Rutas Activas"
        else -> ""
    }

    // Inicializar repositorio para MapStateViewModel
    val context = LocalContext.current
    val repository = remember {
        val database = AppDatabase.getDatabase(context)
        val firestoreService = FirestoreService()
        AppRepository(
            appDao = database.appDao(),
            firestoreService = firestoreService
        )
    }
    
    val mapStateViewModel: MapStateViewModel = viewModel(
        factory = MapStateViewModelFactory(repository)
    )

    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            Routes.STOPS -> {
                mapStateViewModel.showAllStops()
            }

            Routes.ALL_ROUTES -> {}

            Routes.SHARE_LOCATION_SCREEN -> {}
        }
    }

    Scaffold(
        topBar = {
            Toolbar(
                title = screenTitle,
                canNavigateBack = navController.previousBackStackEntry != null &&
                        currentScreen != Routes.STOPS,
                onNavigateUp = { navController.navigateUp() }
            )
        },
        bottomBar = {
            BottomBar(
                items = navigationItems,
                selectedIndex = selectedIndex,
                onItemSelected = { index ->
                    val selectedItem = navigationItems[index]
                    val selectedRoute = selectedItem.route

                    if (selectedRoute != currentScreen && selectedRoute != Routes.SHARE) {

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
                            Routes.SHARE_LOCATION_SCREEN -> {
                                navController.navigate(Routes.SHARE_LOCATION_SCREEN) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (currentScreen == Routes.SHARE_LOCATION_SCREEN) {
            val context = LocalContext.current
            val repository = remember {
                val database = AppDatabase.getDatabase(context)
                val firestoreService = FirestoreService()
                AppRepository(
                    appDao = database.appDao(),
                    firestoreService = firestoreService
                )
            }

            Box(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
            ) {
                ShareLocationScreen(
                    navController = navController,
                    repository = repository
                )
            }
        } else {
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
                    MainContent(padding = innerPaddingSheet, inProduction = false) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            MapContent(mapStateViewModel = mapStateViewModel)
                            SharingStatusIndicator(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .align(androidx.compose.ui.Alignment.TopCenter),
                                onClick = {
                                    navController.navigate(Routes.SHARE_LOCATION_SCREEN) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                },
                sheetContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.5f)
                    ) {
                        AppNavigation(
                            navController = navController,
                            locationViewModel = locationViewModel,
                            mapStateViewModel = mapStateViewModel
                        )
                    }
                },
                sheetPeekHeight = 93.dp,
                sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                sheetDragHandle = {
                    Box(
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(105.dp)
                                .height(9.dp)
                                .padding(top = 4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }
                }
            )
        }
    }
}