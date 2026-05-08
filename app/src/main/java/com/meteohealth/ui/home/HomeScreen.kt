package com.meteohealth.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.meteohealth.domain.model.RiskLevel
import com.meteohealth.feature.home.HomeIntent
import com.meteohealth.feature.home.HomeState
import com.meteohealth.feature.home.HomeViewModel
import com.meteohealth.ui.components.DividedSection
import com.meteohealth.ui.components.MeteoTopBar
import com.meteohealth.ui.theme.NumericStyle
import com.meteohealth.ui.theme.RiskAlert
import com.meteohealth.ui.theme.RiskCalm
import com.meteohealth.ui.theme.RiskHigh
import com.meteohealth.ui.theme.RiskWatch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs

@Composable
fun HomeScreen(onMenuClick: () -> Unit, vm: HomeViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    HomeContent(state, onMenuClick) { vm.dispatch(HomeIntent.Refresh) }
}

@Composable
private fun HomeContent(state: HomeState, onMenuClick: () -> Unit, onRefresh: () -> Unit) {
    Scaffold(
        topBar = {
            MeteoTopBar(
                title = "СЕГОДНЯ",
                onMenuClick = onMenuClick,
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Обновить")
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoading) {
            androidx.compose.foundation.layout.Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ScoreBlock(state)
            state.weather?.let { WeatherBlock(it, state.pressureDelta6h) }
            state.kp?.let { KpBlock(it.kp) }
            if (state.recommendations.isNotEmpty()) RecommendationsBlock(state)
        }
    }
}

@Composable
private fun ScoreBlock(state: HomeState) {
    val riskColor = when (state.riskLevel) {
        RiskLevel.CALM  -> RiskCalm
        RiskLevel.WATCH -> RiskWatch
        RiskLevel.ALERT -> RiskAlert
        RiskLevel.HIGH  -> RiskHigh
    }
    Column(
        Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = state.score.toString(),
            style = NumericStyle,
            color = riskColor,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.verdict,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun WeatherBlock(weather: com.meteohealth.domain.model.WeatherHour, delta: Double) {
    DividedSection("ПОГОДА") {
        MetricRow("T", "${weather.tempC.toInt()}°", "влажность", "${weather.humidity}%")
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        val pressureStr = "${weather.pressureHpa.toInt()} гПа"
        val deltaStr = if (abs(delta) >= 0.1) {
            val sign = if (delta > 0) "+" else ""
            " ($sign${String.format("%.1f", delta)})"
        } else ""
        MetricRow("P", "$pressureStr$deltaStr", "ветер", "${String.format("%.1f", weather.windMps)} м/с")
    }
}

@Composable
private fun KpBlock(kp: Double) {
    DividedSection("МАГНИТЫ") {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Kp ${String.format("%.1f", kp)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f),
            )
            val kpLabel = when {
                kp >= 5 -> "шторм"
                kp >= 4 -> "слабые возмущения"
                else -> "шторма не ожидается"
            }
            Text(kpLabel, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecommendationsBlock(state: HomeState) {
    DividedSection("ЧТО СДЕЛАТЬ") {
        state.recommendations.forEachIndexed { i, rec ->
            Text(
                text = "— ${rec.text}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (i < state.recommendations.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun MetricRow(label1: String, value1: String, label2: String, value2: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label1, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.08f))
        Text(value1, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.weight(0.42f))
        Text(label2, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.25f))
        Text(value2, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.weight(0.25f))
    }
}
