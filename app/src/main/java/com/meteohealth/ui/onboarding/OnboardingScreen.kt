package com.meteohealth.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meteohealth.ui.components.ConditionChip
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Добро пожаловать", style = MaterialTheme.typography.headlineMedium)
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Начать")
        }
    }
}
