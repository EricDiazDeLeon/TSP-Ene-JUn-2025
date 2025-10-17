package com.example.mirutadigital.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mirutadigital.navigation.AppNavigation
import com.example.mirutadigital.navigation.AppScreens
import com.example.mirutadigital.navigation.navigationItems
import com.example.mirutadigital.ui.components.BottomBar
import com.example.mirutadigital.ui.components.MainContent
import com.example.mirutadigital.ui.components.MapContent
import com.example.mirutadigital.ui.components.Toolbar
import com.example.mirutadigital.viewModel.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(locationViewModel: LocationViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val selectedIndex = navigationItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val screenTitle = when (currentRoute) {
        AppScreens.HomeScreen.route -> "Mi Ruta Digital"
        AppScreens.ActiveRoutesScreen.route -> "Rutas Activas"
        else -> ""
    }

    Scaffold(
        topBar = {
            Toolbar(
                title = screenTitle,
                canNavigateBack = navController.previousBackStackEntry != null && currentRoute != AppScreens.HomeScreen.route,
                onNavigateUp = { navController.navigateUp() }
            )
        },
        bottomBar = {
            BottomBar(
                items = navigationItems,
                selectedIndex = selectedIndex,
                onItemSelected = { index ->
                    val selectedRoute = navigationItems[index].route
                    if (selectedRoute != currentRoute && selectedRoute != "share_action") {
                        navController.navigate(selectedRoute) {
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
                    // el mapa ya no se recarga y ahorra llamadas y evita bugs visuales
                    MapContent(padding = innerPaddingSheet)
                }
            },
            sheetContent = {
                Box(modifier = Modifier.fillMaxHeight(0.5f)) {
                    // el NavHost ahora solo controla el contenido de la hoja para mostrar un solo mapa
                    AppNavigation(
                        navController = navController,
                        locationViewModel = locationViewModel
                    )
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