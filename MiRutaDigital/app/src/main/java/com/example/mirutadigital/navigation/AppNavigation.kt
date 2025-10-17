package com.example.mirutadigital.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mirutadigital.viewModel.LocationViewModel
import com.example.mirutadigital.ui.components.NavItem
import com.example.mirutadigital.ui.screens.home.StopsSheetContent
import com.example.mirutadigital.ui.screens.routes.ActiveRoutesSheetContent

sealed class AppScreens(val route: String) {
    object HomeScreen : AppScreens("home_screen")
    //object RoutesScreen : AppScreens("routes_screen")
    object ActiveRoutesScreen : AppScreens("active_routes_screen")
//    object ShareAction : no deberia ir aqui pues es una accion y no una pantalla
//        AppScreens("share_action") // no sera una pantalla, solo un ventana emergente
}

val navigationItems = listOf(
    NavItem("Inicio", Icons.Default.LocationOn, AppScreens.HomeScreen.route),
    NavItem(
        "Ver Rutas",
        Icons.Default.DirectionsBus,
        AppScreens.ActiveRoutesScreen.route
    ), //routes_screen por el momento active...
    NavItem("Compartir", Icons.Default.Groups, "share_action") // no sera una pantalla solo un ventana emergente
)

@Composable
fun AppNavigation(
    navController: NavHostController,
    locationViewModel: LocationViewModel,
//    onShareClick: () -> Unit // para manejar el clic de compartir
) {
    //val navController = rememberNavController() // crea el controlador de navegacion

    NavHost(
        navController = navController,
        startDestination = AppScreens.HomeScreen.route // la pantalla inicial
    ) {
        // la pantalla del inicio
        composable(route = AppScreens.HomeScreen.route) {
            StopsSheetContent(
                viewModel = viewModel(),
                locationViewModel = locationViewModel,
            ) // Le pasamos el navController y ubicacion
        }

        // la pantalla de rutas activas
        composable(route = AppScreens.ActiveRoutesScreen.route) {
            ActiveRoutesSheetContent(
                viewModel = viewModel()
            )
        }
    }
}