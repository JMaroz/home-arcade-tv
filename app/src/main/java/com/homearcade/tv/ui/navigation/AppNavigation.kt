package com.homearcade.tv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.homearcade.tv.HomeArcadeTvApp
import com.homearcade.tv.ui.details.DetailsScreen
import com.homearcade.tv.ui.details.DetailsViewModel
import com.homearcade.tv.ui.home.HomeScreen
import com.homearcade.tv.ui.home.HomeViewModel
import com.homearcade.tv.ui.player.PlayerScreen
import com.homearcade.tv.ui.player.PlayerViewModel
import com.homearcade.tv.ui.profiles.ProfilePickerScreen
import com.homearcade.tv.ui.profiles.ProfileViewModel
import com.homearcade.tv.ui.search.SearchScreen
import com.homearcade.tv.ui.search.SearchViewModel
import com.homearcade.tv.ui.settings.SettingsScreen
import com.homearcade.tv.ui.settings.SettingsViewModel
import com.homearcade.tv.ui.setup.SetupScreen
import com.homearcade.tv.ui.setup.SetupViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String
) {
    val context = LocalContext.current
    val app = context.applicationContext as HomeArcadeTvApp

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1. Setup Screen
        composable(NavRoutes.Setup.route) {
            val setupViewModel = viewModel<SetupViewModel>(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return SetupViewModel(app.preferences) as T
                    }
                }
            )
            SetupScreen(
                viewModel = setupViewModel,
                onConnected = {
                    navController.navigate(NavRoutes.Profiles.route) {
                        popUpTo(NavRoutes.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Profiles Screen
        composable(NavRoutes.Profiles.route) {
            val profileViewModel = viewModel<ProfileViewModel>(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ProfileViewModel(app.profileRepository) as T
                    }
                }
            )
            ProfilePickerScreen(
                viewModel = profileViewModel,
                onProfileSelected = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Profiles.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Home Screen
        composable(NavRoutes.Home.route) {
            val homeViewModel = viewModel<HomeViewModel>(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return HomeViewModel(
                            app.romRepository,
                            app.preferences
                        ) as T
                    }
                }
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToDetails = { romId ->
                    navController.navigate(NavRoutes.Details.createRoute(romId))
                },
                onLaunchGame = { romId ->
                    navController.navigate(NavRoutes.Player.createRoute(romId))
                },
                onSearchClick = {
                    navController.navigate(NavRoutes.Search.route)
                },
                onProfileClick = {
                    navController.navigate(NavRoutes.Profiles.route)
                },
                onSettingsClick = {
                    navController.navigate(NavRoutes.Settings.route)
                }
            )
        }

        // 4. Details Screen
        composable(
            route = NavRoutes.Details.route,
            arguments = listOf(
                navArgument("romId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val romId = backStackEntry.arguments?.getInt("romId") ?: 0

            val detailsViewModel = viewModel<DetailsViewModel>(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return DetailsViewModel(
                            romId = romId,
                            romRepository = app.romRepository,
                            preferences = app.preferences
                        ) as T
                    }
                }
            )

            DetailsScreen(
                viewModel = detailsViewModel,
                onBackClick = { navController.popBackStack() },
                onLaunchGame = { targetRomId, engine ->
                    navController.navigate(NavRoutes.Player.createRoute(targetRomId, engine))
                }
            )
        }

        // 5. Search Screen
        composable(NavRoutes.Search.route) {
            val searchViewModel = viewModel<SearchViewModel>(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return SearchViewModel(app.romRepository, app.preferences) as T
                    }
                }
            )
            SearchScreen(
                viewModel = searchViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToDetails = { romId ->
                    navController.navigate(NavRoutes.Details.createRoute(romId))
                }
            )
        }

        // 6. Settings Screen
        composable(NavRoutes.Settings.route) {
            val settingsViewModel = viewModel<SettingsViewModel>(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return SettingsViewModel(app.preferences, context) as T
                    }
                }
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() },
                onChangeProfile = {
                    navController.navigate(NavRoutes.Profiles.route)
                },
                onServerDisconnected = {
                    navController.navigate(NavRoutes.Setup.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 7. Player Screen
        composable(
            route = NavRoutes.Player.route,
            arguments = listOf(
                navArgument("romId") { type = NavType.IntType },
                navArgument("engine") { type = NavType.StringType; defaultValue = "webrtc" },
                navArgument("discId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val romId = backStackEntry.arguments?.getInt("romId") ?: 0
            val engine = backStackEntry.arguments?.getString("engine") ?: "webrtc"
            val discId = backStackEntry.arguments?.getInt("discId") ?: -1

            val playerViewModel = viewModel<PlayerViewModel>(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return PlayerViewModel(
                            romId = romId,
                            engine = engine,
                            discId = discId,
                            preferences = app.preferences
                        ) as T
                    }
                }
            )

            PlayerScreen(
                viewModel = playerViewModel,
                onExit = { navController.popBackStack() }
            )
        }
    }
}
