package com.meteohealth.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

private data class NavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val items = listOf(
    NavItem(NavRoutes.DASHBOARD, "Главная", Icons.Default.Home),
    NavItem(NavRoutes.FORECAST, "Прогноз", Icons.Default.Cloud),
    NavItem(NavRoutes.DIARY, "Дневник", Icons.Default.Book),
    NavItem(NavRoutes.RECOMMENDATIONS, "Советы", Icons.Default.Lightbulb),
    NavItem(NavRoutes.SETTINGS, "Настройки", Icons.Default.Settings)
)

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
