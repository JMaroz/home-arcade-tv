package com.homearcade.tv.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Setup : NavRoutes("setup")
    data object Profiles : NavRoutes("profiles")
    data object Home : NavRoutes("home")
    data object Search : NavRoutes("search")
    data object Settings : NavRoutes("settings")

    data object Details : NavRoutes("details/{romId}") {
        fun createRoute(romId: Int): String = "details/$romId"
    }

    data object Player : NavRoutes("player/{romId}?engine={engine}&discId={discId}") {
        fun createRoute(
            romId: Int,
            engine: String = "webrtc",
            discId: Int = -1
        ): String {
            return "player/$romId?engine=$engine&discId=$discId"
        }
    }
}
