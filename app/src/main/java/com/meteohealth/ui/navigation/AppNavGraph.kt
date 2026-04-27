package com.meteohealth.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.meteohealth.ui.onboarding.OnboardingScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.DASHBOARD) {
            // TODO: DashboardScreen
            androidx.compose.material3.Text("Dashboard")
        }
        composable(NavRoutes.FORECAST) {
            // TODO: ForecastScreen
            androidx.compose.material3.Text("Forecast")
        }
        composable(NavRoutes.DIARY) {
            // TODO: DiaryScreen
            androidx.compose.material3.Text("Diary")
        }
        composable(NavRoutes.RECOMMENDATIONS) {
            // TODO: RecommendationsScreen
            androidx.compose.material3.Text("Recommendations")
        }
        composable(NavRoutes.SETTINGS) {
            // TODO: SettingsScreen
            androidx.compose.material3.Text("Settings")
        }
    }
}
