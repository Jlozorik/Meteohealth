package com.meteohealth.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteohealth.domain.model.WeatherSnapshot
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = if (state.profile.name.isNotBlank()) "Привет, ${state.profile.name}" else "Метео-здоровье",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = state.weather?.cityName ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            actions = {
                IconButton(onClick = viewModel::refresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                }
            }
        )

        if (state.isLoading && state.weather == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.wellbeingIndex?.let { index ->
                WellbeingCard(index = index)
            }
            state.weather?.let { weather ->
                WeatherCard(weather = weather)
            }
            state.kpIndex?.let { kp ->
                KpCard(kpIndex = kp)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun WellbeingCard(index: Int) {
    val (color, label) = when {
        index >= 80 -> MaterialTheme.colorScheme.primary to "Хорошо"
        index >= 60 -> Color(0xFFF9A825) to "Умеренно"
        index >= 40 -> Color(0xFFEF6C00) to "Плохо"
        else -> MaterialTheme.colorScheme.error to "Опасно"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "$index",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Column {
                Text("Индекс самочувствия", style = MaterialTheme.typography.labelMedium)
                Text(label, style = MaterialTheme.typography.headlineSmall, color = color)
                Text(
                    "из 100",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeatherCard(weather: WeatherSnapshot) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${weather.temperatureCelsius.toInt()}°C",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = weather.weatherDescription.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val fmt = SimpleDateFormat("HH:mm, d MMMM", Locale("ru"))
            Text(
                text = "Обновлено: ${fmt.format(Date(weather.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                WeatherParam("Давление", "${weather.pressureHpa.toInt()} гПа")
                WeatherParam("Влажность", "${weather.humidity}%")
                WeatherParam("Ветер", "${weather.windSpeedMs} м/с")
            }
        }
    }
}

@Composable
private fun KpCard(kpIndex: Float) {
    val (color, label) = when {
        kpIndex < 3 -> MaterialTheme.colorScheme.primary to "Спокойно"
        kpIndex < 5 -> Color(0xFFF9A825) to "Умеренно"
        kpIndex < 7 -> Color(0xFFEF6C00) to "Активно"
        else -> MaterialTheme.colorScheme.error to "Буря"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Геомагнитная активность", style = MaterialTheme.typography.labelMedium)
                Text(label, style = MaterialTheme.typography.titleMedium, color = color)
            }
            Text(
                text = "Kp ${String.format("%.1f", kpIndex)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun WeatherParam(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
