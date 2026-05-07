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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteohealth.domain.WellbeingCalculator
import com.meteohealth.domain.model.PressureUnit
import com.meteohealth.domain.model.WeatherSnapshot
import com.meteohealth.domain.model.toDisplayPressure
import com.meteohealth.ui.components.SeverityBadge
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
                state.wellbeingIndex?.let { idx ->
                    WellbeingCard(
                        index = idx,
                        severity = state.severity
                    )
                }
                state.weather?.let {
                    WeatherCard(
                        weather = it,
                        pressureUnit = state.profile.pressureUnit,
                        pressureDelta6h = state.pressureDelta6h,
                        tempDelta24h = state.tempDelta24h
                    )
                }
                KpCard(
                    kpIndex = state.kpIndex,
                    kpPenalty = state.breakdown?.kpPenalty ?: 0
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun WellbeingCard(index: Int, severity: WellbeingCalculator.Severity) {
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
    val shortText = when {
        index >= 80 -> "низкий риск"
        index >= 60 -> "умеренный риск"
        index >= 40 -> "повышенный риск"
        else        -> "высокий риск"
    }
    val animatedColor by animateColorAsState(targetColor, tween(600), label = "wellbeingColor")
    val animatedIndex by animateIntAsState(index, tween(800), label = "wellbeingIndex")
    val tenPoint = (index / 10).coerceIn(0, 10)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = targetColor.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
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
            // Компактный «виджет» X/10 (правка 2.3 Pravka2)
            Text(
                text = "$tenPoint из 10 — $shortText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Светофор «зелёный/жёлтый/красный» (правка 2.1 Pravka2)
            SeverityBadge(severity = severity)
        }
    }
}

@Composable
private fun WeatherCard(
    weather: WeatherSnapshot,
    pressureUnit: PressureUnit,
    pressureDelta6h: Float,
    tempDelta24h: Float
) {
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Thermostat,
                        contentDescription = null,
                        tint = FactorThresholds.temperatureDelta(tempDelta24h).toColor(),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "${weather.temperatureCelsius.toInt()}°C",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = weather.weatherDescription.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val fmt = SimpleDateFormat("HH:mm, d MMMM", Locale.forLanguageTag("ru"))
            Text(
                text = "Обновлено: ${fmt.format(Date(weather.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                WeatherParam(
                    icon = Icons.Outlined.Compress,
                    iconTint = FactorThresholds.pressureDelta(pressureDelta6h).toColor(),
                    label = "Давление",
                    value = weather.pressureHpa.toDisplayPressure(pressureUnit),
                    modifier = Modifier.weight(1f)
                )
                WeatherParam(
                    icon = Icons.Outlined.WaterDrop,
                    iconTint = FactorThresholds.humidity(weather.humidity).toColor(),
                    label = "Влажность",
                    value = "${weather.humidity}%",
                    modifier = Modifier.weight(1f)
                )
                WeatherParam(
                    icon = Icons.Outlined.Air,
                    iconTint = FactorThresholds.wind(weather.windSpeedMs).toColor(),
                    label = "Ветер",
                    value = "${weather.windSpeedMs} м/с",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KpCard(kpIndex: Float?, kpPenalty: Int) {
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    val (color, label) = when {
        kpIndex == null -> placeholderColor to "Нет данных"
        kpIndex < 3     -> MaterialTheme.colorScheme.primary to "Спокойно"
        kpIndex < 5     -> WellbeingModerate to "Умеренно"
        kpIndex < 7     -> WellbeingPoor to "Активно"
        else            -> WellbeingDanger to "Буря"
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Bolt,
                    contentDescription = null,
                    tint = animatedColor,
                    modifier = Modifier.size(28.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Геомагнитная активность",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(label, style = MaterialTheme.typography.titleMedium, color = animatedColor)
                    if (kpIndex != null && kpPenalty > 0) {
                        Text(
                            text = "вклад в индекс: −$kpPenalty балл${pluralBalls(kpPenalty)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                text = if (kpIndex != null) "Kp ${String.format(Locale.ROOT, "%.1f", kpIndex)}" else "Kp —",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
        }
    }
}

private fun pluralBalls(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod10 == 1 && mod100 != 11 -> ""
        mod10 in 2..4 && (mod100 < 12 || mod100 > 14) -> "а"
        else -> "ов"
    }
}

@Composable
private fun WeatherParam(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
