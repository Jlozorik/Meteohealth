package com.meteohealth.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.meteohealth.ui.dashboard.DashboardScreen
import com.meteohealth.ui.diary.DiaryAddScreen
import com.meteohealth.ui.diary.DiaryListScreen
import com.meteohealth.ui.forecast.ForecastScreen
import com.meteohealth.ui.onboarding.OnboardingScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            val hideBar = currentRoute == NavRoutes.ONBOARDING || currentRoute == NavRoutes.DIARY_ADD
            if (!hideBar) {
                BottomNavBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
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
                DashboardScreen()
            }
            composable(NavRoutes.FORECAST) {
                ForecastScreen()
            }
            composable(NavRoutes.DIARY) {
                DiaryListScreen(onAddClick = { navController.navigate(NavRoutes.DIARY_ADD) })
            }
            composable(NavRoutes.DIARY_ADD) {
                DiaryAddScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.RECOMMENDATIONS) {
                androidx.compose.material3.Text("Советы — скоро")
            }
            composable(NavRoutes.SETTINGS) {
                androidx.compose.material3.Text("Настройки — скоро")
            }
        }
    }
}
