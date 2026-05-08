package com.meteohealth.ui.journal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.meteohealth.domain.model.Symptom
import com.meteohealth.domain.triggers.CorrelationStrength
import com.meteohealth.domain.triggers.PearsonAnalyzer
import com.meteohealth.feature.journal.JournalIntent
import com.meteohealth.feature.journal.JournalState
import com.meteohealth.feature.journal.JournalTab
import com.meteohealth.feature.journal.JournalViewModel
import com.meteohealth.ui.components.DividedSection
import com.meteohealth.ui.components.EmptyState
import com.meteohealth.ui.components.MeteoTopBar
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(onMenuClick: () -> Unit, vm: JournalViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    JournalContent(state, onMenuClick) { vm.dispatch(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JournalContent(state: JournalState, onMenuClick: () -> Unit, onIntent: (JournalIntent) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            MeteoTopBar(
                title = "ЖУРНАЛ",
                onMenuClick = onMenuClick,
                actions = {
                    if (state.tab == JournalTab.ENTRIES) {
                        IconButton(onClick = { onIntent(JournalIntent.OpenSheet) }) {
                            Icon(Icons.Outlined.Add, contentDescription = "Отметить")
                        }
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(state.tab) { onIntent(JournalIntent.TabSelected(it)) }
            when (state.tab) {
                JournalTab.ENTRIES -> EntriesTab(state, onIntent)
                JournalTab.TRIGGERS -> TriggersTab(state)
            }
        }

        if (state.isSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { onIntent(JournalIntent.CloseSheet) },
                sheetState = sheetState,
            ) {
                AddEntrySheet(state, onIntent)
            }
        }
    }
}

@Composable
private fun TabRow(current: JournalTab, onSelect: (JournalTab) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        FilterChip(
            selected = current == JournalTab.ENTRIES,
            onClick = { onSelect(JournalTab.ENTRIES) },
            label = { Text("ЗАПИСИ", style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.weight(1f).padding(end = 4.dp),
        )
        FilterChip(
            selected = current == JournalTab.TRIGGERS,
            onClick = { onSelect(JournalTab.TRIGGERS) },
            label = { Text("ТРИГГЕРЫ", style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.weight(1f).padding(start = 4.dp),
        )
    }
}

@Composable
private fun EntriesTab(state: JournalState, onIntent: (JournalIntent) -> Unit) {
    if (state.entries.isEmpty()) {
        EmptyState("Дневник пуст.")
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(state.entries, key = { it.id }) { entry ->
            val fmt = SimpleDateFormat("dd.MM EE HH:mm", Locale.getDefault())
            val dots = "●".repeat(entry.level) + "○".repeat(5 - entry.level)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fmt.format(Date(entry.ts)),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.weight(1f),
                        )
                        Text(dots, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    if (entry.symptoms.isNotEmpty()) {
                        Text(
                            text = entry.symptoms.joinToString(", ") { it.name.lowercase().replace('_', ' ') },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = { onIntent(JournalIntent.DeleteEntry(entry.id)) }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Стереть",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun TriggersTab(state: JournalState) {
    if (state.entries.size < 14) {
        EmptyState("Мало записей. ${state.entries.size} из 14.")
        return
    }
    DividedSection("ФАКТОРЫ") {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("ФАКТОР", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.35f))
            Text("r", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.20f))
            Text("КОЛ-ВО", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.25f))
            Text("СВЯЗЬ", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.20f))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        state.triggers.forEach { trigger ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(trigger.factor, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.35f))
                Text(String.format("%.2f", trigger.r),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.weight(0.20f))
                Text("${trigger.sampleCount}", style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(0.25f))
                Text(strengthLabel(PearsonAnalyzer.strength(trigger.r)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.20f))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

private fun strengthLabel(s: CorrelationStrength) = when (s) {
    CorrelationStrength.STRONG   -> "сильная"
    CorrelationStrength.MODERATE -> "средняя"
    CorrelationStrength.WEAK     -> "слабая"
    CorrelationStrength.NONE     -> "нет"
}

@Composable
private fun AddEntrySheet(state: JournalState, onIntent: (JournalIntent) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text("Отметить", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Text("Самочувствие: ${state.draftLevel}", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = state.draftLevel.toFloat(),
            onValueChange = { onIntent(JournalIntent.DraftLevelChanged(it.toInt())) },
            valueRange = 1f..5f,
            steps = 3,
        )
        Spacer(Modifier.height(8.dp))
        Text("Симптомы", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf(Symptom.HEADACHE, Symptom.FATIGUE, Symptom.PRESSURE, Symptom.DIZZINESS).forEach { s ->
                FilterChip(
                    selected = s in state.draftSymptoms,
                    onClick = { onIntent(JournalIntent.DraftSymptomToggled(s)) },
                    label = { Text(s.name.lowercase(), style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.draftNotes,
            onValueChange = { onIntent(JournalIntent.DraftNotesChanged(it)) },
            placeholder = { Text("Заметка") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onIntent(JournalIntent.SubmitEntry) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Сохранить") }
        Spacer(Modifier.height(16.dp))
    }
}
