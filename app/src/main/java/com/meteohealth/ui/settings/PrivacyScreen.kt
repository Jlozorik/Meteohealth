package com.meteohealth.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    viewModel: PrivacyViewModel = koinViewModel()
) {
    var showWipeDialog by remember { mutableStateOf(false) }
    var wiped by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Конфиденциальность", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Section("Где хранятся данные") {
                "Все ваши данные — профиль, дневник самочувствия, история погоды и журнал уведомлений — " +
                    "хранятся локально на устройстве в зашифрованной базе данных Room. " +
                    "Они не передаются третьим лицам и не покидают устройство без вашего действия."
            }
            Section("Какие данные мы используем") {
                "Приложение запрашивает текущую погоду у OpenWeatherMap по координатам " +
                    "(город или геопозиция, заданные в настройках) и Kp-индекс у NOAA SWPC. " +
                    "В этих запросах не передаются персональные данные — только координаты."
            }
            Section("Геолокация") {
                "Координаты используются только для запроса погоды. Доступ к геолокации запрашивается " +
                    "разово, фоновое отслеживание не используется. Вы можете в любой момент удалить " +
                    "координаты в настройках или отозвать разрешение в системных настройках Android."
            }
            Section("Уведомления") {
                "Локальные уведомления формируются на устройстве. Их содержание не передаётся на сервер."
            }
            Section("Соответствие законодательству") {
                "Приложение соответствует требованиям 152-ФЗ «О персональных данных» (РФ) и GDPR (ЕС) " +
                    "в части локального хранения и пользовательского контроля над данными."
            }

            OutlinedButton(
                onClick = { showWipeDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
            ) {
                Text("Удалить все данные", style = MaterialTheme.typography.bodyMedium)
            }
            if (wiped) {
                Text(
                    "Все локальные данные удалены.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = { Text("Удалить все данные?") },
            text = { Text("Профиль, дневник, история погоды и журнал уведомлений будут удалены без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.wipeAllData { wiped = true }
                    showWipeDialog = false
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showWipeDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun Section(title: String, body: () -> String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            body(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
