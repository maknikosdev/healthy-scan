package com.healthyscan.app.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")

    object Home : Screen("home")
    object Scan : Screen("scan")
    object Favorites : Screen("favorites")
    object History : Screen("history")
    object Profile : Screen("profile")

    object ProductResult : Screen("product/{barcode}?record={record}") {
        /** [record] = true for an actual fresh barcode scan (adds a History
         *  row). Pass false when reopening a product the person already saw
         *  before (History/Favorites/Home) so it doesn't count as a new scan. */
        fun createRoute(barcode: String, record: Boolean = true) = "product/$barcode?record=$record"
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
    object EditPreferences : Screen("edit_preferences")

    companion object {
        const val ARG_BARCODE = "barcode"
        const val ARG_NAME = "name"
        const val ARG_BRAND = "brand"
        const val ARG_RECORD = "record"
    }
}

/** The 5 destinations shown in the bottom navigation bar. */
val bottomNavScreens = listOf(Screen.Home, Screen.Scan, Screen.Favorites, Screen.History, Screen.Profile)
