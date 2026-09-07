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

    object AddProduct : Screen("add_product/{barcode}?name={name}&brand={brand}") {
        fun createRoute(barcode: String, name: String = "", brand: String = ""): String {
            val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
            val encodedBrand = java.net.URLEncoder.encode(brand, "UTF-8")
            return "add_product/$barcode?name=$encodedName&brand=$encodedBrand"
        }
    }

    object Basket : Screen("basket")
    object Compare : Screen("compare")
    object Premium : Screen("premium")

    companion object {
        const val ARG_BARCODE = "barcode"
        const val ARG_NAME = "name"
        const val ARG_BRAND = "brand"
    }
}

/** The 5 destinations shown in the bottom navigation bar. */
val bottomNavScreens = listOf(Screen.Home, Screen.Scan, Screen.Favorites, Screen.History, Screen.Profile)
