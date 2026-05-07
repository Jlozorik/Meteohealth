package com.meteohealth.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.meteohealth.domain.model.Sensitivity
import com.meteohealth.ui.components.CityPickerSection
import com.meteohealth.ui.components.ConditionChip
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            viewModel.detectLocation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Добро пожаловать", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(
            "Расскажите немного о себе, чтобы приложение учитывало ваши особенности.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Ваше имя (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Возраст (необязательно)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = state.age?.toString() ?: "—",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }
        }
        Slider(
            value = (state.age ?: 30).toFloat(),
            onValueChange = { viewModel.onAgeChange(it.toInt()) },
            valueRange = 18f..90f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Text("Тип метеочувствительности", style = MaterialTheme.typography.titleSmall)
        val sensitivities = listOf(
            Sensitivity.LIGHT to "Лёгкая",
            Sensitivity.MODERATE to "Средняя",
            Sensitivity.STRONG to "Сильная"
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            sensitivities.forEachIndexed { index, (value, label) ->
                SegmentedButton(
                    selected = state.sensitivity == value,
                    onClick = { viewModel.onSensitivityChange(value) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = sensitivities.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.primary,
                        activeBorderColor = MaterialTheme.colorScheme.primary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                ) { Text(label) }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text("Где вы находитесь?", style = MaterialTheme.typography.titleMedium)
        Text(
            "Нужно для прогноза погоды и индекса самочувствия именно для вашего города.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CityPickerSection(
            selectedCity = state.cityName,
            onCitySelected = viewModel::onCitySelected,
            onSearch = { viewModel.searchCity(it) },
            onDetectClick = {
                val fineGranted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val coarseGranted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (fineGranted || coarseGranted) {
                    viewModel.detectLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            statusLine = state.locationStatus,
            isDetecting = state.isDetectingLocation
        )

        Spacer(Modifier.height(8.dp))

        Text("Хронические состояния", style = MaterialTheme.typography.titleMedium)
        Text(
            "Отметьте, если это про вас — мы будем точнее предупреждать об опасных условиях.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ConditionChip(
            label = "Гипертония / сердце",
            selected = state.hasHypertension,
            onClick = viewModel::onHypertensionToggle
        )
        ConditionChip(
            label = "Мигрени / головные боли",
            selected = state.hasMigraines,
            onClick = viewModel::onMigrainesToggle
        )
        ConditionChip(
            label = "Боли в суставах",
            selected = state.hasJointPain,
            onClick = viewModel::onJointPainToggle
        )
        ConditionChip(
            label = "Дыхательные заболевания",
            selected = state.hasRespiratoryIssues,
            onClick = viewModel::onRespiratoryToggle
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.finish(onFinish) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = state.locationSelected
        ) {
            Text("Начать", fontWeight = FontWeight.SemiBold)
        }
        if (!state.locationSelected) {
            Text(
                "Выберите город или определите автоматически, чтобы продолжить.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                "Город можно изменить позже в настройках.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
