package com.meteohealth.ui.forecast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteohealth.domain.model.ForecastDay
import com.meteohealth.ui.theme.WellbeingDanger
import com.meteohealth.ui.theme.WellbeingGood
import com.meteohealth.ui.theme.WellbeingModerate
import com.meteohealth.ui.theme.WellbeingPoor
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.PointConnector
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(viewModel: ForecastViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Прогноз", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            state.error -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Не удалось загрузить прогноз",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> PullToRefreshBox(
                isRefreshing = false,
                onRefresh = viewModel::load,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(state.days) { _, day ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                        ) {
                            ForecastDayCard(
                                day = day,
                                personalIndex = state.personalIndices[day.dateMillis]
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastDayCard(day: ForecastDay, personalIndex: Int? = null) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    LaunchedEffect(day) {
        modelProducer.runTransaction {
            lineSeries { series(day.slots.map { it.temperatureCelsius.toDouble() }) }
        }
    }

    val wellbeingColor = when {
        day.wellbeingIndex >= 80 -> WellbeingGood
        day.wellbeingIndex >= 60 -> WellbeingModerate
        day.wellbeingIndex >= 40 -> WellbeingPoor
        else                     -> WellbeingDanger
    }

    val avgHumidity = remember(day) { day.slots.map { it.humidity }.average().toInt() }
    val avgWindMs = remember(day) { day.slots.map { it.windSpeedMs }.average() }
    val hasChart = day.slots.size >= 2
    val timeRange = remember(day) {
        if (day.slots.size < 2) "" else {
            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            "${fmt.format(Date(day.slots.first().timestamp))} – ${fmt.format(Date(day.slots.last().timestamp))}"
        }
    }

    val timeFormatter = remember(day) {
        CartesianValueFormatter { _, value, _ ->
            val idx = value.toInt().coerceIn(0, day.slots.lastIndex)
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(day.slots[idx].timestamp))
        }
    }

    val markerLabel = rememberTextComponent(
        color = MaterialTheme.colorScheme.onSurface,
        textSize = 11.sp,
    )
    val markerValueFormatter = remember {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            targets.filterIsInstance<LineCartesianLayerMarkerTarget>()
                .flatMap { it.points }
                .firstOrNull()
                ?.let { "${it.entry.y.toInt()}°C" }
                ?: ""
        }
    }
    val marker = rememberDefaultCartesianMarker(
        label = markerLabel,
        valueFormatter = markerValueFormatter,
    )

    val smoothLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(primaryColor)),
        stroke = LineCartesianLayer.LineStroke.continuous(thickness = 2.dp),
        areaFill = LineCartesianLayer.AreaFill.single(
            fill(ShaderProvider.verticalGradient(arrayOf(primaryColor.copy(alpha = 0.35f), Color.Transparent)))
        ),
        pointConnector = PointConnector.cubic(curvature = 0.5f),
    )

    val axisLabel = rememberAxisLabelComponent(
        color = onSurfaceVariantColor,
        textSize = 10.sp,
    )
    val bottomAxis = HorizontalAxis.rememberBottom(
        label = axisLabel,
        line = null,
        tick = null,
        guideline = null,
        valueFormatter = timeFormatter,
        itemPlacer = remember { HorizontalAxis.ItemPlacer.aligned() },
    )

    val chart = rememberCartesianChart(
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(smoothLine)
        ),
        bottomAxis = bottomAxis,
        marker = marker,
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = wellbeingColor.copy(alpha = 0.18f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Заголовок: дата + бейджи самочувствия
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(day.dateMillis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (personalIndex != null) PersonalIndexBadge(personalIndex)
                    WellbeingBadge(day.wellbeingIndex, wellbeingColor)
                }
            }

            // Температура + описание погоды
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${day.minTempCelsius.toInt()}° / ${day.maxTempCelsius.toInt()}°C",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                day.slots.firstOrNull()?.let { slot ->
                    Text(
                        text = slot.weatherDescription.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Доп. метеопараметры + диапазон времени на графике
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "💧 $avgHumidity%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "💨 ${"%.1f".format(avgWindMs)} м/с",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (timeRange.isNotEmpty()) {
                    Text(
                        text = timeRange,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // График температуры — только если слотов >= 2 (ошибка если смотреть вечером график (с 5 до 9 текущего дня)
            if (hasChart) {
                CartesianChartHost(
                    chart = chart,
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
            } else {
                Text(
                    text = "График появится позже — данных за временной интервал мало",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun WellbeingBadge(index: Int, color: Color) {
    Text(
        text = "$index",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
private fun PersonalIndexBadge(index: Int) {
    val color = when {
        index >= 80 -> WellbeingGood
        index >= 60 -> WellbeingModerate
        index >= 40 -> WellbeingPoor
        else        -> WellbeingDanger
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "личный",
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.8f)
        )
        Text(
            text = "$index",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun formatDate(millis: Long): String {
    val fmt = SimpleDateFormat("EEEE, d MMMM", Locale.forLanguageTag("ru"))
    return fmt.format(Date(millis)).replaceFirstChar { it.uppercase() }
}
