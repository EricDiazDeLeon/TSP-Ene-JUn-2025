package com.example.mirutadigital.navigation

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mirutadigital.data.local.AppDatabase
import com.example.mirutadigital.data.remote.FirestoreService
import com.example.mirutadigital.data.repository.AppRepository
import com.example.mirutadigital.ui.components.NavItem
import com.example.mirutadigital.ui.screens.home.StopsScreen
import com.example.mirutadigital.ui.screens.home.StopsViewModelFactory
import com.example.mirutadigital.ui.screens.routes.RoutesContainerScreen
import com.example.mirutadigital.ui.screens.shareLocation.ShareLocationScreen
import com.example.mirutadigital.viewModel.LocationViewModel
import com.example.mirutadigital.viewModel.MapStateViewModel

object Routes {
    const val STOPS = "stops_screen"
    const val ALL_ROUTES = "all_routes_screen"
    //const val ACTIVE_ROUTES = "active_routes_screen"
    const val SHARE = "share_action"
    const val SHARE_LOCATION_SCREEN = "share_location_screen"
}

sealed class AppScreens(val route: String) {
    object StopsScreen : AppScreens(Routes.STOPS)

    object AllRoutesScreen : AppScreens(Routes.ALL_ROUTES) {
        const val ROUTE_WITH_ARGS = "${Routes.ALL_ROUTES}?stopId={stopId}"

        fun createRoute(stopId: String? = null): String {
            return if (stopId != null) {
                "${Routes.ALL_ROUTES}?stopId=$stopId"
            } else {
                Routes.ALL_ROUTES
            }
        }
    }

    object ShareLocationScreen : AppScreens(Routes.SHARE_LOCATION_SCREEN)
    //object ActiveRoutesScreen : AppScreens(Routes.ACTIVE_ROUTES)
    //    object ShareAction : no deberia ir aqui pues es una accion y no una pantalla
//        AppScreens("share_action") // no sera una pantalla, solo un ventana emergente
}

val navigationItems = listOf(
    NavItem("Inicio", Icons.Default.LocationOn, Routes.STOPS),
    NavItem("Ver Rutas", Icons.Default.DirectionsBus, Routes.ALL_ROUTES),
    NavItem("Compartir", Icons.Default.Groups, Routes.SHARE_LOCATION_SCREEN)
)

@Composable
fun AppNavigation(
    navController: NavHostController,
    locationViewModel: LocationViewModel,
    mapStateViewModel: MapStateViewModel
) {
    // Inicializar repositorio y servicios
    val context = LocalContext.current
    
    val repository = remember {
        val database = AppDatabase.getDatabase(context)
        val firestoreService = FirestoreService()
        AppRepository(
            appDao = database.appDao(),
            firestoreService = firestoreService
        )
    }

    NavHost(
        navController = navController,
        startDestination = Routes.STOPS
    ) {
        composable(route = Routes.STOPS) {
            StopsScreen(
                viewModel = viewModel(factory = StopsViewModelFactory(repository)),
                locationViewModel = locationViewModel,
                mapStateViewModel = mapStateViewModel,
                navController = navController
            )
        }

        composable(
            route = AppScreens.AllRoutesScreen.ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument("stopId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val stopId = backStackEntry.arguments?.getString("stopId")

            RoutesContainerScreen(
                repository = repository,
                mapStateViewModel = mapStateViewModel,
                filteredStopId = stopId
            )
        }

        composable(route = Routes.SHARE_LOCATION_SCREEN) {
            ShareLocationScreen(
                navController = navController,
                repository = repository
            )
        }
    }
}