package com.healthyscan.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.healthyscan.app.R
import com.healthyscan.app.ui.navigation.Screen
import com.healthyscan.app.ui.navigation.bottomNavScreens

@Composable
fun HealthyScanBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar {
        bottomNavScreens.forEach { screen ->
            val (icon, labelRes) = when (screen) {
                Screen.Home -> Icons.Filled.Home to R.string.nav_home
                Screen.Scan -> Icons.Filled.CameraAlt to R.string.nav_scan
                Screen.Favorites -> Icons.Filled.Favorite to R.string.nav_favorites
                Screen.History -> Icons.Filled.History to R.string.nav_history
                Screen.Profile -> Icons.Filled.Person to R.string.nav_profile
                else -> Icons.Filled.Home to R.string.nav_home
            }
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen) },
                icon = { Icon(icon, contentDescription = stringResource(labelRes)) },
                label = { Text(stringResource(labelRes)) },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}
