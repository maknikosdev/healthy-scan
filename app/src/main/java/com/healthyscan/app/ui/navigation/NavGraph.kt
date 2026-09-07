package com.healthyscan.app.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.healthyscan.app.data.repository.ProductRepository
import com.healthyscan.app.data.repository.SettingsRepository
import com.healthyscan.app.locale.LocaleManager
import com.healthyscan.app.ui.components.HealthyScanBottomBar
import com.healthyscan.app.ui.screens.addproduct.AddProductScreen
import com.healthyscan.app.ui.screens.favorites.FavoritesScreen
import com.healthyscan.app.ui.screens.history.HistoryScreen
import com.healthyscan.app.ui.screens.home.HomeScreen
import com.healthyscan.app.ui.screens.home.SearchScreen
import com.healthyscan.app.ui.screens.onboarding.OnboardingScreen
import com.healthyscan.app.ui.screens.premium.PremiumScreen
import com.healthyscan.app.ui.screens.product.ProductResultScreen
import com.healthyscan.app.ui.screens.profile.ProfileScreen
import com.healthyscan.app.ui.screens.scan.LabelScanScreen
import com.healthyscan.app.ui.screens.scan.ScanScreen
import com.healthyscan.app.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun HealthyScanNavHost(
    productRepository: ProductRepository,
    settingsRepository: SettingsRepository
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val onboardingDone by settingsRepository.onboardingDone.collectAsState(initial = true)
    val dietaryPreferences by settingsRepository.dietaryPreferences.collectAsState(initial = emptySet())
    val avoidedAllergens by settingsRepository.avoidedAllergens.collectAsState(initial = emptySet())
    val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val systemDark = isSystemInDarkTheme()
    val isDarkMode = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    var currentLanguage by remember { mutableStateOf(LocaleManager.currentLanguage()) }
    val onToggleLanguage: () -> Unit = {
        LocaleManager.toggleLanguage()
        currentLanguage = LocaleManager.currentLanguage()
    }
    val onToggleTheme: () -> Unit = {
        val next = if (isDarkMode) ThemeMode.LIGHT else ThemeMode.DARK
        scope.launch { settingsRepository.setThemeMode(next) }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavScreens.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                HealthyScanBottomBar(currentRoute = currentRoute) { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingDone) Screen.Home.route else Screen.Onboarding.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(onFinished = { selectedPrefs ->
                    scope.launch {
                        settingsRepository.setDietaryPreferences(selectedPrefs)
                        settingsRepository.setOnboardingDone(true)
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    productRepository = productRepository,
                    isDarkMode = isDarkMode,
                    currentLanguage = currentLanguage,
                    onToggleTheme = onToggleTheme,
                    onToggleLanguage = onToggleLanguage,
                    onScanBarcode = { navController.navigate(Screen.Scan.route) },
                    onSearch = { navController.navigate("search") },
                    onScanLabel = { navController.navigate("label_scan") },
                    onOpenProduct = { barcode ->
                        navController.navigate(Screen.ProductResult.createRoute(barcode))
                    }
                )
            }

            composable(Screen.Scan.route) {
                ScanScreen(onBarcodeDetected = { barcode ->
                    navController.navigate(Screen.ProductResult.createRoute(barcode)) {
                        popUpTo(Screen.Scan.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    productRepository = productRepository,
                    isDarkMode = isDarkMode,
                    currentLanguage = currentLanguage,
                    onToggleTheme = onToggleTheme,
                    onToggleLanguage = onToggleLanguage,
                    onOpenProduct = { barcode -> navController.navigate(Screen.ProductResult.createRoute(barcode)) }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    productRepository = productRepository,
                    isDarkMode = isDarkMode,
                    currentLanguage = currentLanguage,
                    onToggleTheme = onToggleTheme,
                    onToggleLanguage = onToggleLanguage,
                    onOpenProduct = { barcode -> navController.navigate(Screen.ProductResult.createRoute(barcode)) }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    productRepository = productRepository,
                    settingsRepository = settingsRepository,
                    isDarkMode = isDarkMode,
                    currentLanguage = currentLanguage,
                    onToggleTheme = onToggleTheme,
                    onToggleLanguage = onToggleLanguage,
                    onOpenPremium = { navController.navigate(Screen.Premium.route) }
                )
            }

            composable(
                route = Screen.ProductResult.route,
                arguments = listOf(navArgument(Screen.ARG_BARCODE) { type = NavType.StringType })
            ) { entry ->
                val barcode = entry.arguments?.getString(Screen.ARG_BARCODE).orEmpty()
                ProductResultScreen(
                    barcode = barcode,
                    productRepository = productRepository,
                    avoidedAllergens = avoidedAllergens,
                    userDietaryPreferences = dietaryPreferences,
                    onBack = { navController.popBackStack() },
                    onOpenAlternative = { altBarcode ->
                        navController.navigate(Screen.ProductResult.createRoute(altBarcode))
                    },
                    onScanLabel = { navController.navigate("label_scan") },
                    onAddProduct = { name, brand ->
                        navController.navigate(Screen.AddProduct.createRoute(barcode, name, brand))
                    }
                )
            }

            composable(
                route = Screen.AddProduct.route,
                arguments = listOf(
                    navArgument(Screen.ARG_BARCODE) { type = NavType.StringType },
                    navArgument(Screen.ARG_NAME) { type = NavType.StringType; defaultValue = "" },
                    navArgument(Screen.ARG_BRAND) { type = NavType.StringType; defaultValue = "" }
                )
            ) { entry ->
                val barcode = entry.arguments?.getString(Screen.ARG_BARCODE).orEmpty()
                val name = entry.arguments?.getString(Screen.ARG_NAME).orEmpty()
                val brand = entry.arguments?.getString(Screen.ARG_BRAND).orEmpty()
                AddProductScreen(
                    barcode = barcode,
                    productRepository = productRepository,
                    settingsRepository = settingsRepository,
                    prefilledName = name,
                    prefilledBrand = brand,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("search") {
                SearchScreen(
                    productRepository = productRepository,
                    onBack = { navController.popBackStack() },
                    onOpenProduct = { barcode -> navController.navigate(Screen.ProductResult.createRoute(barcode)) }
                )
            }

            composable("label_scan") {
                LabelScanScreen(onDone = { navController.popBackStack() })
            }

            composable(Screen.Premium.route) {
                PremiumScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
