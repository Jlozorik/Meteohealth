package com.meteohealth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.meteohealth.ui.MainViewModel
import com.meteohealth.ui.navigation.AppNavGraph
import com.meteohealth.ui.theme.MeteohealthTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = koinViewModel()
            val startDestination by viewModel.startDestination.collectAsState()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MeteohealthTheme(darkTheme = isDarkTheme) {
                if (startDestination != null) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        startDestination = startDestination!!
                    )
                } else {
                    Box(Modifier.fillMaxSize())
                }
            }
        }
    }
}
