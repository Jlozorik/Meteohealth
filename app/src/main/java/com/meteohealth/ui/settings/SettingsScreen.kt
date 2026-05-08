package com.meteohealth.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meteohealth.domain.model.PressureUnit
import com.meteohealth.feature.settings.SettingsIntent
import com.meteohealth.feature.settings.SettingsState
import com.meteohealth.feature.settings.SettingsViewModel
import com.meteohealth.ui.components.DividedSection
import com.meteohealth.ui.components.MeteoTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(onMenuClick: () -> Unit, vm: SettingsViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    SettingsContent(state, onMenuClick) { vm.dispatch(it) }
}

@Composable
private fun SettingsContent(state: SettingsState, onMenuClick: () -> Unit, onIntent: (SettingsIntent) -> Unit) {
    Scaffold(
        topBar = { MeteoTopBar(title = "НАСТРОЙКИ", onMenuClick = onMenuClick) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            NotifSection(state, onIntent)
            UnitsSection(state, onIntent)
            CitySection(state, onIntent)
            SourcesSection(state, onIntent)
            PrivacySection(state, onIntent)
            DataSection(state, onIntent)
            AboutSection()
        }
    }
    if (state.showClearConfirm) {
        AlertDialog(
            onDismissRequest = { onIntent(SettingsIntent.DismissClearConfirm) },
            title = { Text("Стереть всё?") },
            text = { Text("Все записи дневника будут удалены без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = { onIntent(SettingsIntent.ConfirmClear) }) { Text("Стереть") }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(SettingsIntent.DismissClearConfirm) }) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun NotifSection(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    DividedSection("УВЕДОМЛЕНИЯ") {
        listOf("main" to "Главные", "pressure" to "Давление", "kp" to "Геомагнитная активность").forEach { (kind, label) ->
            val enabled = state.profile.notificationPrefs[kind] ?: true
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = enabled, onCheckedChange = { onIntent(SettingsIntent.NotifToggled(kind, it)) })
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun UnitsSection(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    DividedSection("ЕДИНИЦЫ") {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Давление", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = {
                val next = if (state.profile.pressureUnit == PressureUnit.HPA) PressureUnit.MM_HG else PressureUnit.HPA
                onIntent(SettingsIntent.PressureUnitChanged(next))
            }) {
                Text(if (state.profile.pressureUnit == PressureUnit.HPA) "гПа" else "мм рт.ст.")
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Чувствительность: ${state.profile.sensitivity}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
        Slider(
            value = state.profile.sensitivity.toFloat(),
            onValueChange = { onIntent(SettingsIntent.SensitivityChanged(it.toInt())) },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun CitySection(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    DividedSection("ГОРОД") {
        Row(
            Modifier.fillMaxWidth().clickable { onIntent(SettingsIntent.ToggleSection("city")) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                state.profile.city.ifEmpty { "Не выбран" },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text("Изменить", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)
        }
        AnimatedVisibility(visible = state.expandedSection == "city") {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = state.cityQuery,
                    onValueChange = { onIntent(SettingsIntent.CityQueryChanged(it)) },
                    placeholder = { Text("Введите город") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (state.cityQuery.length >= 2) {
                    TextButton(onClick = { onIntent(SettingsIntent.CitySelected(state.cityQuery, 0.0, 0.0)) }) {
                        Text(state.cityQuery)
                    }
                }
            }
        }
    }
}

@Composable
private fun SourcesSection(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    DividedSection("ОТКУДА ДАННЫЕ") {
        Row(
            Modifier.fillMaxWidth().clickable { onIntent(SettingsIntent.ToggleSection("sources")) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text("Источники данных", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(if (state.expandedSection == "sources") "▲" else "▼",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AnimatedVisibility(visible = state.expandedSection == "sources") {
            Text(
                "Погода: OpenWeatherMap (api.openweathermap.org).\nГеомагнитная активность: NOAA SWPC (services.swpc.noaa.gov).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun PrivacySection(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    DividedSection("ПРИВАТНОСТЬ") {
        Row(
            Modifier.fillMaxWidth().clickable { onIntent(SettingsIntent.ToggleSection("privacy")) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text("Данные и приватность", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(if (state.expandedSection == "privacy") "▲" else "▼",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AnimatedVisibility(visible = state.expandedSection == "privacy") {
            Text(
                "Все данные хранятся локально на устройстве. Сторонним сервисам передаются только координаты для получения погоды.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun DataSection(state: SettingsState, onIntent: (SettingsIntent) -> Unit) {
    DividedSection("ДАННЫЕ") {
        Button(
            onClick = { onIntent(SettingsIntent.RequestClearConfirm) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        ) { Text("Стереть всё") }
    }
}

@Composable
private fun AboutSection() {
    DividedSection("О ПРИЛОЖЕНИИ") {
        Text(
            "Meteohealth — приложение для метеозависимых.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}
