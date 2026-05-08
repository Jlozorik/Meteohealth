package com.meteohealth.ui.forecast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.meteohealth.feature.forecast.DayRow
import com.meteohealth.feature.forecast.ForecastIntent
import com.meteohealth.feature.forecast.ForecastState
import com.meteohealth.feature.forecast.ForecastViewModel
import com.meteohealth.ui.components.DividedSection
import com.meteohealth.ui.components.EmptyState
import com.meteohealth.ui.components.MeteoTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun ForecastScreen(onMenuClick: () -> Unit, vm: ForecastViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    ForecastContent(state, onMenuClick) { vm.dispatch(it) }
}

@Composable
private fun ForecastContent(
    state: ForecastState,
    onMenuClick: () -> Unit,
    onIntent: (ForecastIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            MeteoTopBar(title = "ПРОГНОЗ", onMenuClick = onMenuClick)
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.days.isEmpty() && !state.isLoading) {
            EmptyState("Нет данных.", Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            DividedSection("ПРОГНОЗ НА 5 ДНЕЙ") {
                ForecastTableHeader()
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                state.days.forEachIndexed { i, day ->
                    DayRowItem(
                        day = day,
                        pressureUnit = state.pressureUnit,
                        onClick = { onIntent(ForecastIntent.ToggleDay(i)) },
                    )
                    if (day.expanded) {
                        HourlyTable(day.hours, state.pressureUnit)
                    }
                    if (i < state.days.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastTableHeader() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell("ДАТА", 0.15f)
        HeaderCell("T мин/макс", 0.30f)
        HeaderCell("P", 0.20f)
        HeaderCell("Kp", 0.15f)
        HeaderCell("РИСК", 0.20f)
    }
}

@Composable
private fun DayRowItem(day: DayRow, pressureUnit: com.meteohealth.domain.model.PressureUnit, onClick: () -> Unit) {
    val pressureStr = if (pressureUnit == com.meteohealth.domain.model.PressureUnit.MM_HG) {
        "${(day.pressureHpa * 0.750062).toInt()} мм"
    } else {
        "${day.pressureHpa.toInt()} гПа"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonoCell(day.dateLabel, 0.15f)
        MonoCell("${tempStr(day.tempMin)}/${tempStr(day.tempMax)}", 0.30f)
        MonoCell(pressureStr, 0.20f)
        MonoCell(String.format("%.1f", day.kp), 0.15f)
        Text(day.riskLabel, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.20f))
    }
}

@Composable
private fun HourlyTable(hours: List<com.meteohealth.domain.model.WeatherHour>, pressureUnit: com.meteohealth.domain.model.PressureUnit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp)
    ) {
        hours.forEach { h ->
            val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(h.hourBucketEpoch * 3600 * 1000))
            val p = if (pressureUnit == com.meteohealth.domain.model.PressureUnit.MM_HG) {
                "${(h.pressureHpa * 0.750062).toInt()} мм"
            } else "${h.pressureHpa.toInt()} гПа"
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MonoCell(time, 0.15f)
                MonoCell("${tempStr(h.tempC)}", 0.20f)
                MonoCell(p, 0.30f)
                MonoCell("${h.humidity}%", 0.15f)
                MonoCell("${String.format("%.1f", h.windMps)} м/с", 0.20f)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun HeaderCell(text: String, weight: Float) =
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(weight),
    )

@Composable
private fun MonoCell(text: String, weight: Float) =
    Text(
        text,
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        modifier = Modifier.weight(weight),
    )

private fun tempStr(t: Double): String {
    val sign = if (t > 0) "+" else ""
    return "$sign${t.toInt()}°"
}
