package com.meteohealth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.meteohealth.ui.MainViewModel
import com.meteohealth.ui.navigation.AppNavGraph
import com.meteohealth.ui.navigation.NavRoutes
import com.meteohealth.ui.theme.MeteohealthTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = koinViewModel()
            val realStartDestination by viewModel.startDestination.collectAsState()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MeteohealthTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    startDestination = NavRoutes.SPLASH,
                    realStartDestination = realStartDestination
                )
            }
        }
    }
}
