package com.meteohealth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.meteohealth.domain.usecase.ObserveProfileUseCase
import com.meteohealth.ui.components.MeteoDrawer
import com.meteohealth.ui.forecast.ForecastScreen
import com.meteohealth.ui.home.HomeScreen
import com.meteohealth.ui.journal.JournalScreen
import com.meteohealth.ui.navigation.NavRoutes
import com.meteohealth.ui.onboarding.OnboardingScreen
import com.meteohealth.ui.settings.SettingsScreen
import com.meteohealth.ui.theme.MeteoTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val observeProfile: ObserveProfileUseCase by inject()

    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge()
        setContent {
            MeteoTheme {
                MeteoApp(observeProfile)
            }
        }
    }
}

@Composable
private fun MeteoApp(observeProfile: ObserveProfileUseCase) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route ?: NavRoutes.HOME

    val profile by observeProfile().collectAsState(initial = null)
    val startDestination = if (profile == null) NavRoutes.ONBOARDING else NavRoutes.HOME

    LaunchedEffect(startDestination) { }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MeteoDrawer(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(NavRoutes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) {
        NavHost(navController = navController, startDestination = startDestination) {
            composable(NavRoutes.ONBOARDING) {
                OnboardingScreen(onFinish = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                })
            }
            composable(NavRoutes.HOME) {
                HomeScreen(onMenuClick = { scope.launch { drawerState.open() } })
            }
            composable(NavRoutes.FORECAST) {
                ForecastScreen(onMenuClick = { scope.launch { drawerState.open() } })
            }
            composable(NavRoutes.JOURNAL) {
                JournalScreen(onMenuClick = { scope.launch { drawerState.open() } })
            }
            composable(NavRoutes.SETTINGS) {
                SettingsScreen(onMenuClick = { scope.launch { drawerState.open() } })
            }
        }
    }
}
