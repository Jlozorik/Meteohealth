package com.meteohealth.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteohealth.domain.model.PressureUnit
import com.meteohealth.domain.model.WeatherSnapshot
import com.meteohealth.domain.model.toDisplayPressure
import com.meteohealth.ui.theme.WellbeingDanger
import com.meteohealth.ui.theme.WellbeingGood
import com.meteohealth.ui.theme.WellbeingModerate
import com.meteohealth.ui.theme.WellbeingPoor
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (state.profile.name.isNotBlank()) "Привет, ${state.profile.name}" else "Метео-здоровье",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (state.weather?.cityName?.isNotBlank() == true) {
                            Text(
                                text = state.weather!!.cityName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading && state.weather == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            return@Scaffold
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                state.wellbeingIndex?.let { WellbeingCard(it) }
                state.weather?.let { WeatherCard(it, state.profile.pressureUnit) }
                state.kpIndex?.let { KpCard(it) }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun WellbeingCard(index: Int) {
    val targetColor = when {
        index >= 80 -> WellbeingGood
        index >= 60 -> WellbeingModerate
        index >= 40 -> WellbeingPoor
        else        -> WellbeingDanger
    }
    val label = when {
        index >= 80 -> "Хорошо"
        index >= 60 -> "Умеренно"
        index >= 40 -> "Плохо"
        else        -> "Опасно"
    }
    val animatedColor by animateColorAsState(targetColor, tween(600), label = "wellbeingColor")
    val animatedIndex by animateIntAsState(index, tween(800), label = "wellbeingIndex")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = targetColor.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AnimatedContent(
                targetState = animatedIndex,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                label = "indexAnim"
            ) { idx ->
                Text(
                    text = "$idx",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = animatedColor
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Индекс самочувствия",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(label, style = MaterialTheme.typography.headlineSmall, color = animatedColor)
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
private fun WeatherCard(weather: WeatherSnapshot, pressureUnit: PressureUnit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${weather.temperatureCelsius.toInt()}°C",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
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

            Row(modifier = Modifier.fillMaxWidth()) {
                WeatherParam("Давление", weather.pressureHpa.toDisplayPressure(pressureUnit), Modifier.weight(1f))
                WeatherParam("Влажность", "${weather.humidity}%", Modifier.weight(1f))
                WeatherParam("Ветер", "${weather.windSpeedMs} м/с", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun KpCard(kpIndex: Float) {
    val (color, label) = when {
        kpIndex < 3 -> MaterialTheme.colorScheme.primary to "Спокойно"
        kpIndex < 5 -> WellbeingModerate to "Умеренно"
        kpIndex < 7 -> WellbeingPoor to "Активно"
        else        -> WellbeingDanger to "Буря"
    }
    val animatedColor by animateColorAsState(color, tween(600), label = "kpColor")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Геомагнитная активность",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(label, style = MaterialTheme.typography.titleMedium, color = animatedColor)
            }
            Text(
                text = "Kp ${String.format("%.1f", kpIndex)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
        }
    }
}

@Composable
private fun WeatherParam(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
