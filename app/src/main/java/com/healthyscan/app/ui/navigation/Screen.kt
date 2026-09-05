package com.healthyscan.app.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")

    object Home : Screen("home")
    object Scan : Screen("scan")
    object Favorites : Screen("favorites")
    object History : Screen("history")
    object Profile : Screen("profile")

    object ProductResult : Screen("product/{barcode}") {
        fun createRoute(barcode: String) = "product/$barcode"
    }

    object Basket : Screen("basket")
    object Compare : Screen("compare")
    object Premium : Screen("premium")

    companion object {
        const val ARG_BARCODE = "barcode"
    }
}

/** The 5 destinations shown in the bottom navigation bar. */
val bottomNavScreens = listOf(Screen.Home, Screen.Scan, Screen.Favorites, Screen.History, Screen.Profile)
