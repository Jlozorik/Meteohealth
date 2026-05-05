package com.meteohealth.ui.settings

import androidx.compose.foundation.layout.Arrangement
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
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meteohealth.domain.model.PressureUnit
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenSources: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var cityField by remember(profile.cityName) { mutableStateOf(profile.cityName.orEmpty()) }
    var locationStatus by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        if (granted) {
            viewModel.detectLocation { ok ->
                locationStatus = if (ok) "Координаты определены" else "Не удалось получить координаты"
            }
        } else {
            locationStatus = "Доступ к геолокации не выдан"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) },
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            SectionHeader("Уведомления")

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Уведомления о погоде", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(
                        "Оповещения при неблагоприятных условиях",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = profile.notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }

            if (profile.notificationsEnabled) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Какие события показывать",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                NotificationToggleRow(
                    title = "Скачки давления",
                    checked = profile.notifyPressureJump,
                    onCheckedChange = { viewModel.setNotifyPressureJump(it) }
                )
                NotificationToggleRow(
                    title = "Магнитные бури",
                    checked = profile.notifyGeomagneticStorm,
                    onCheckedChange = { viewModel.setNotifyGeomagneticStorm(it) }
                )
                NotificationToggleRow(
                    title = "Морозы",
                    checked = profile.notifyFrost,
                    onCheckedChange = { viewModel.setNotifyFrost(it) }
                )
                NotificationToggleRow(
                    title = "Жара",
                    checked = profile.notifyHeat,
                    onCheckedChange = { viewModel.setNotifyHeat(it) }
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))

            SectionHeader("Внешний вид")

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Тёмная тема", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(
                        if (profile.isDarkTheme) "Включена" else "Выключена",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = profile.isDarkTheme,
                    onCheckedChange = { viewModel.setDarkTheme(it) }
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))

            SectionHeader("Единицы измерения")
            Spacer(Modifier.height(12.dp))

            Text(
                "Давление",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(PressureUnit.MMHG to "мм рт.ст.", PressureUnit.HPA to "гПа")
                    .forEachIndexed { index, (unit, label) ->
                        SegmentedButton(
                            selected = profile.pressureUnit == unit,
                            onClick = { viewModel.setPressureUnit(unit) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.primary,
                                activeBorderColor = MaterialTheme.colorScheme.primary,
                                inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            )
                        ) {
                            Text(label)
                        }
                    }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))

            SectionHeader("Местоположение")
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = cityField,
                onValueChange = { cityField = it },
                label = { Text("Город") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
            LaunchedEffect(cityField) { viewModel.setCityName(cityField) }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val fineGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val coarseGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (fineGranted || coarseGranted) {
                        viewModel.detectLocation { ok ->
                            locationStatus = if (ok) "Координаты определены" else "Не удалось получить координаты"
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Определить автоматически", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(6.dp))
            val coordsLine = profile.latitude?.let { lat ->
                profile.longitude?.let { lon -> "Координаты: %.3f, %.3f".format(lat, lon) }
            }
            Text(
                locationStatus ?: coordsLine ?: "Координаты не заданы — будет использоваться Москва",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))

            SectionHeader("Данные")
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                )
            ) {
                Text("Очистить дневник", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Все записи дневника будут удалены без возможности восстановления.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))

            SectionHeader("О приложении")
            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onOpenSources,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Источники", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            }
            TextButton(
                onClick = onOpenPrivacy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Конфиденциальность", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Очистить дневник?") },
            text = { Text("Все записи будут удалены. Это действие необратимо.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearDiary()
                    showClearDialog = false
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}
