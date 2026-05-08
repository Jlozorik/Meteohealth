package com.meteohealth.feature.journal

object JournalReducer {
    fun reduce(state: JournalState, intent: JournalIntent): Pair<JournalState, List<JournalEffect>> =
        when (intent) {
            is JournalIntent.TabSelected -> state.copy(tab = intent.tab) to emptyList()
            is JournalIntent.EntriesArrived -> state.copy(entries = intent.entries, isLoading = false) to emptyList()
            is JournalIntent.TriggersArrived -> state.copy(triggers = intent.results) to emptyList()
            is JournalIntent.OpenSheet -> state.copy(isSheetOpen = true) to emptyList()
            is JournalIntent.CloseSheet -> state.copy(
                isSheetOpen = false,
                draftLevel = 3,
                draftNotes = "",
                draftSymptoms = emptySet(),
            ) to emptyList()
            is JournalIntent.DraftLevelChanged -> state.copy(draftLevel = intent.level) to emptyList()
            is JournalIntent.DraftNotesChanged -> state.copy(draftNotes = intent.notes) to emptyList()
            is JournalIntent.DraftSymptomToggled -> {
                val updated = if (intent.symptom in state.draftSymptoms) {
                    state.draftSymptoms - intent.symptom
                } else {
                    state.draftSymptoms + intent.symptom
                }
                state.copy(draftSymptoms = updated) to emptyList()
            }
            is JournalIntent.SubmitEntry -> state.copy(
                isSheetOpen = false,
                draftLevel = 3,
                draftNotes = "",
                draftSymptoms = emptySet(),
            ) to listOf(JournalEffect.EntryAdded)
            is JournalIntent.DeleteEntry -> state to emptyList()
        }
}
