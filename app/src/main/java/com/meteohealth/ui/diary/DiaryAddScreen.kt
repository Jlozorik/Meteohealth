package com.meteohealth.ui.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meteohealth.domain.model.WellbeingLevel
import org.koin.androidx.compose.koinViewModel

private val SYMPTOMS = listOf(
    "Головная боль",
    "Усталость",
    "Высокое давление",
    "Боль в суставах",
    "Тревожность",
    "Слабость",
    "Головокружение"
)

private val WELLBEING_OPTIONS = listOf(
    WellbeingLevel.TERRIBLE to "Ужасно",
    WellbeingLevel.POOR to "Плохо",
    WellbeingLevel.FAIR to "Нормально",
    WellbeingLevel.GOOD to "Хорошо",
    WellbeingLevel.GREAT to "Отлично"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiaryAddScreen(
    onBack: () -> Unit,
    viewModel: DiaryViewModel = koinViewModel()
) {
    var selectedLevel by remember { mutableStateOf(WellbeingLevel.FAIR) }
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая запись") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Самочувствие", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WELLBEING_OPTIONS.forEach { (level, label) ->
                    FilterChip(
                        selected = selectedLevel == level,
                        onClick = { selectedLevel = level },
                        label = { Text(label) }
                    )
                }
            }

            Text("Симптомы", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SYMPTOMS.forEach { symptom ->
                    FilterChip(
                        selected = symptom in selectedSymptoms,
                        onClick = {
                            if (symptom in selectedSymptoms) selectedSymptoms.remove(symptom)
                            else selectedSymptoms.add(symptom)
                        },
                        label = { Text(symptom) }
                    )
                }
            }

            Text("Заметки", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Как вы себя чувствуете?") },
                minLines = 3
            )

            Button(
                onClick = {
                    viewModel.addEntry(selectedLevel, selectedSymptoms.toList(), notes)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}
