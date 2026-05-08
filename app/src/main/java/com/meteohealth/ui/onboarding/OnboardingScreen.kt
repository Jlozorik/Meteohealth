package com.meteohealth.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.meteohealth.feature.onboarding.OnboardingEffect
import com.meteohealth.feature.onboarding.OnboardingIntent
import com.meteohealth.feature.onboarding.OnboardingState
import com.meteohealth.feature.onboarding.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

private val CONDITIONS = listOf("мигрень", "гипертония", "гипотония", "метеозависимость", "астма")

@Composable
fun OnboardingScreen(onFinish: () -> Unit, vm: OnboardingViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.effects.collect { if (it is OnboardingEffect.Finished) onFinish() }
    }

    OnboardingContent(state) { vm.dispatch(it) }
}

@Composable
private fun OnboardingContent(state: OnboardingState, onIntent: (OnboardingIntent) -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(32.dp))
            StepIndicator(state.step, total = 4)
            Spacer(Modifier.height(24.dp))

            when (state.step) {
                0 -> Step0(state, onIntent)
                1 -> Step1(state, onIntent)
                2 -> Step2(state, onIntent)
                3 -> Step3(state, onIntent)
            }

            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth()) {
                if (state.step > 0) {
                    TextButton(onClick = { onIntent(OnboardingIntent.Back) }) { Text("Назад") }
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = { onIntent(OnboardingIntent.Next) }) {
                    Text(if (state.step == 3) "Готово" else "Далее")
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(current: Int, total: Int) {
    Text(
        text = "${current + 1} / $total",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Step0(state: OnboardingState, onIntent: (OnboardingIntent) -> Unit) {
    Text("Кратко о себе.", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(24.dp))
    OutlinedTextField(
        value = state.name,
        onValueChange = { onIntent(OnboardingIntent.NameChanged(it)) },
        label = { Text("Имя") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = state.age,
        onValueChange = { onIntent(OnboardingIntent.AgeChanged(it)) },
        label = { Text("Возраст") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun Step1(state: OnboardingState, onIntent: (OnboardingIntent) -> Unit) {
    Text("Насколько чувствуешь.", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(8.dp))
    Text(
        "Чувствительность: ${state.sensitivity}",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Slider(
        value = state.sensitivity.toFloat(),
        onValueChange = { onIntent(OnboardingIntent.SensitivityChanged(it.toInt())) },
        valueRange = 1f..5f,
        steps = 3,
    )
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth()) {
        Text("слабо", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text("сильно", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Step2(state: OnboardingState, onIntent: (OnboardingIntent) -> Unit) {
    Text("Хронические состояния.", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(16.dp))
    CONDITIONS.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth()) {
            row.forEach { cond ->
                FilterChip(
                    selected = cond in state.conditions,
                    onClick = { onIntent(OnboardingIntent.ConditionToggled(cond)) },
                    label = { Text(cond, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun Step3(state: OnboardingState, onIntent: (OnboardingIntent) -> Unit) {
    Text("Твой город.", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(24.dp))
    OutlinedTextField(
        value = state.city,
        onValueChange = { onIntent(OnboardingIntent.CityChanged(it)) },
        label = { Text("Город") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}
