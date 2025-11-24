package com.example.mirutadigital.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mirutadigital.ui.components.NavItem
import com.example.mirutadigital.ui.screens.destinationSearch.DestinationSearchScreen
import com.example.mirutadigital.ui.screens.home.StopsScreen
import com.example.mirutadigital.ui.screens.menu.favorites.FavoritesScreen
import com.example.mirutadigital.ui.screens.menu.history.HistoryScreen
import com.example.mirutadigital.ui.screens.routes.RoutesContainerScreen
import com.example.mirutadigital.ui.screens.routes.detailRoute.RouteDetailScreen
import com.example.mirutadigital.ui.screens.share.ShareScreen
import com.example.mirutadigital.viewModel.LocationViewModel
import com.example.mirutadigital.viewModel.MapStateViewModel

object Routes {
    const val STOPS = "stops_screen"
    const val ALL_ROUTES = "all_routes_screen"
    const val DESTINATION_SEARCH = "destination_search_screen"
    const val SHARE = "share_screen"
    const val ROUTE_DETAIL = "route_detail_screen"
    const val FAVORITES = "favorites_screen"
    const val HISTORY = "history_screen"
    const val STREET_VIEW = "street_view_screen"
}

sealed class AppScreens(val route: String) {
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

//    object DestinationSearchScreen : AppScreens(Routes.DESTINATION_SEARCH)

    object RouteDetailScreen : AppScreens(Routes.ROUTE_DETAIL) {
        const val ROUTE_ID_ARG = "routeId"
        const val ROUTE_WITH_ARGS = "${Routes.ROUTE_DETAIL}/{${ROUTE_ID_ARG}}"

        fun createRoute(routeId: String): String {
            return "${Routes.ROUTE_DETAIL}/$routeId"
        }
    }

    object StreetViewScreen : AppScreens(Routes.STREET_VIEW) {
        const val COORDS_ARG = "coords"
        const val ROUTE_WITH_ARGS = "${Routes.STREET_VIEW}/{${COORDS_ARG}}"

        fun createRoute(lat: Double, lng: Double): String {
            return "${Routes.STREET_VIEW}/${lat},${lng}"
        }
    }

//    object FavoritesScreen : AppScreens(Routes.FAVORITES)
//    object HistoryScreen : AppScreens(Routes.HISTORY)
}

val navigationItems = listOf(
    NavItem("Inicio", Icons.Default.LocationOn, Routes.STOPS),
    NavItem("Ver Rutas", Icons.Default.DirectionsBus, Routes.ALL_ROUTES),
    NavItem("Buscar", Icons.Default.Search, Routes.DESTINATION_SEARCH),
    NavItem("Compartir", Icons.Default.Groups, Routes.SHARE)
)

@Composable
fun AppNavigation(
    navController: NavHostController,
    locationViewModel: LocationViewModel,
    mapStateViewModel: MapStateViewModel,
    isSheetExpanded: Boolean,
    onExpandSheet: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.STOPS
    ) {
        composable(route = Routes.STOPS) {
            StopsScreen(
                viewModel = viewModel(),
                locationViewModel = locationViewModel,
                mapStateViewModel = mapStateViewModel,
                navController = navController,
                isSheetExpanded = isSheetExpanded,
                onExpandSheet = onExpandSheet
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
                mapStateViewModel = mapStateViewModel,
                filteredStopId = stopId,
                onViewRouteDetail = { routeId ->
                    navController.navigate(AppScreens.RouteDetailScreen.createRoute(routeId))
                },
                isSheetExpanded = isSheetExpanded,
                onExpandSheet = onExpandSheet
            )
        }

        composable(route = Routes.DESTINATION_SEARCH) {
            DestinationSearchScreen(
                navController = navController,
                mapStateViewModel = mapStateViewModel,
                isSheetExpanded = isSheetExpanded,
                onExpandSheet = onExpandSheet,
                viewModel = viewModel()
            )
        }

        composable(
            route = AppScreens.RouteDetailScreen.ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(AppScreens.RouteDetailScreen.ROUTE_ID_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val routeId =
                backStackEntry.arguments?.getString(AppScreens.RouteDetailScreen.ROUTE_ID_ARG)!!

            RouteDetailScreen(
                routeId = routeId,
                navController = navController,
                mapStateViewModel = mapStateViewModel,
                viewModel = viewModel(),
                onExpandSheet = onExpandSheet,
                onNavigateBack = {
                    navController.navigate(Routes.ALL_ROUTES) {
                        popUpTo(Routes.ALL_ROUTES) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = AppScreens.StreetViewScreen.ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(AppScreens.StreetViewScreen.COORDS_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val coordsString =
                backStackEntry.arguments?.getString(AppScreens.StreetViewScreen.COORDS_ARG) ?: ""

            com.example.mirutadigital.ui.screens.streetview.StreetViewScreen(
                coords = coordsString
            )
        }

        composable(route = Routes.FAVORITES) {
            FavoritesScreen(
                navController = navController,
                viewModel = viewModel()
            )
        }

        composable(route = Routes.HISTORY) {
            HistoryScreen(
                viewModel = viewModel()
            )
        }

        composable(Routes.SHARE) {
            ShareScreen(
                navController = navController,
                locationViewModel = locationViewModel,
                mapStateViewModel = mapStateViewModel,
                isSheetExpanded = isSheetExpanded,
                onExpandSheet = onExpandSheet
            )
        }
    }
}
