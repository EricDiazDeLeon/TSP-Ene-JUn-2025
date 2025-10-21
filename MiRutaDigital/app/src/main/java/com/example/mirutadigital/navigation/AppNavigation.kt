package com.example.mirutadigital.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mirutadigital.data.local.AppDatabase
import com.example.mirutadigital.data.remote.FirestoreService
import com.example.mirutadigital.data.repository.AppRepository
import com.example.mirutadigital.ui.components.NavItem
import com.example.mirutadigital.ui.screens.home.HomeScreen
import com.example.mirutadigital.ui.screens.home.HomeScreenViewModel
import com.example.mirutadigital.ui.screens.home.HomeScreenViewModelFactory
import com.example.mirutadigital.ui.screens.routes.RoutesScreen
import com.example.mirutadigital.ui.screens.shareLocation.ShareLocationScreen
import com.example.mirutadigital.ui.screens.shareLocation.ShareLocationViewModelFactory

sealed class AppScreens(val route: String) {
    object HomeScreen : AppScreens("home_screen")
    object RoutesScreen : AppScreens("routes_screen")
    object ShareLocationScreen : AppScreens("share_location_screen")
}

val navigationItems = listOf(
    NavItem("Inicio", Icons.Default.LocationOn, "home_screen"),
    NavItem("Ver Rutas", Icons.Default.DirectionsBus, "routes_screen"), //routes_screen
    NavItem("Compartir", Icons.Default.Groups, "share_location_screen")
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 🔹 Crear las dependencias necesarias del repositorio
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val appDao = database.appDao()
    val firestoreService = FirestoreService()
    val repository = AppRepository(appDao, firestoreService)

    // 🔹 Crear el ViewModel con el Factory
    val homeViewModel: HomeScreenViewModel = viewModel(
        factory = HomeScreenViewModelFactory(repository)
    )

    NavHost(
        navController = navController,
        startDestination = AppScreens.HomeScreen.route
    ) {
        composable(route = AppScreens.HomeScreen.route) {
            HomeScreen(navController = navController, viewModel = homeViewModel)
        }
        composable(route = AppScreens.RoutesScreen.route) {
            RoutesScreen(navController = navController, repository = repository)
        }
        composable(route = AppScreens.ShareLocationScreen.route) {
            ShareLocationScreen(navController = navController, repository = repository)
        }
    }
}
