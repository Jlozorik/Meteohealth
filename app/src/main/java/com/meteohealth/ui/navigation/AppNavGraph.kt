package com.meteohealth.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.meteohealth.ui.diary.TriggersScreen
import com.meteohealth.ui.forecast.ForecastScreen
import com.meteohealth.ui.onboarding.OnboardingScreen
import com.meteohealth.ui.recommendations.RecommendationsScreen
import com.meteohealth.ui.settings.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            val hideBar = currentRoute == NavRoutes.ONBOARDING
                || currentRoute == NavRoutes.DIARY_ADD
                || currentRoute == NavRoutes.TRIGGERS
            if (!hideBar) {
                BottomNavBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            // Только bottom padding — каждый экран сам обрабатывает системные insets через свой Scaffold/TopAppBar
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 12 } },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it / 12 } }
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
            composable(NavRoutes.DASHBOARD) { DashboardScreen() }
            composable(NavRoutes.FORECAST) { ForecastScreen() }
            composable(NavRoutes.DIARY) {
                DiaryListScreen(
                    onAddClick = { navController.navigate(NavRoutes.DIARY_ADD) },
                    onTriggersClick = { navController.navigate(NavRoutes.TRIGGERS) }
                )
            }
            composable(NavRoutes.TRIGGERS) {
                TriggersScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.DIARY_ADD) {
                DiaryAddScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.RECOMMENDATIONS) { RecommendationsScreen() }
            composable(NavRoutes.SETTINGS) { SettingsScreen() }
        }
    }
}
