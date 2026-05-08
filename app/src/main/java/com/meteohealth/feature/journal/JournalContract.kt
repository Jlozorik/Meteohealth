package com.meteohealth.feature.journal

import com.meteohealth.domain.model.JournalEntry
import com.meteohealth.domain.model.Symptom
import com.meteohealth.domain.triggers.TriggerResult

enum class JournalTab { ENTRIES, TRIGGERS }

data class JournalState(
    val tab: JournalTab = JournalTab.ENTRIES,
    val entries: List<JournalEntry> = emptyList(),
    val triggers: List<TriggerResult> = emptyList(),
    val isSheetOpen: Boolean = false,
    val draftLevel: Int = 3,
    val draftNotes: String = "",
    val draftSymptoms: Set<Symptom> = emptySet(),
    val isLoading: Boolean = true,
)

sealed interface JournalIntent {
    data class TabSelected(val tab: JournalTab) : JournalIntent
    data class EntriesArrived(val entries: List<JournalEntry>) : JournalIntent
    data class TriggersArrived(val results: List<TriggerResult>) : JournalIntent
    data object OpenSheet : JournalIntent
    data object CloseSheet : JournalIntent
    data class DraftLevelChanged(val level: Int) : JournalIntent
    data class DraftNotesChanged(val notes: String) : JournalIntent
    data class DraftSymptomToggled(val symptom: Symptom) : JournalIntent
    data object SubmitEntry : JournalIntent
    data class DeleteEntry(val id: Long) : JournalIntent
}

sealed interface JournalEffect {
    data object EntryAdded : JournalEffect
}
