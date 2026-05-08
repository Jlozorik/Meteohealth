package com.meteohealth.feature.journal

import com.meteohealth.domain.model.Symptom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalReducerTest {

    private val initial = JournalState()

    @Test fun tab_selected_changes_tab() {
        val (state, _) = JournalReducer.reduce(initial, JournalIntent.TabSelected(JournalTab.TRIGGERS))
        assertEquals(JournalTab.TRIGGERS, state.tab)
    }

    @Test fun open_sheet_sets_flag() {
        val (state, _) = JournalReducer.reduce(initial, JournalIntent.OpenSheet)
        assertTrue(state.isSheetOpen)
    }

    @Test fun close_sheet_resets_draft() {
        val withDraft = initial.copy(isSheetOpen = true, draftLevel = 5, draftNotes = "test",
            draftSymptoms = setOf(Symptom.HEADACHE))
        val (state, _) = JournalReducer.reduce(withDraft, JournalIntent.CloseSheet)
        assertFalse(state.isSheetOpen)
        assertEquals(3, state.draftLevel)
        assertEquals("", state.draftNotes)
        assertTrue(state.draftSymptoms.isEmpty())
    }

    @Test fun symptom_toggle_adds_then_removes() {
        var state = initial
        val (s1, _) = JournalReducer.reduce(state, JournalIntent.DraftSymptomToggled(Symptom.HEADACHE))
        assertTrue(Symptom.HEADACHE in s1.draftSymptoms)
        val (s2, _) = JournalReducer.reduce(s1, JournalIntent.DraftSymptomToggled(Symptom.HEADACHE))
        assertFalse(Symptom.HEADACHE in s2.draftSymptoms)
    }

    @Test fun submit_emits_effect_and_resets_draft() {
        val withDraft = initial.copy(draftLevel = 4, draftNotes = "голова", isSheetOpen = true)
        val (state, effects) = JournalReducer.reduce(withDraft, JournalIntent.SubmitEntry)
        assertFalse(state.isSheetOpen)
        assertEquals(3, state.draftLevel)
        assertEquals(1, effects.size)
        assertTrue(effects.first() is JournalEffect.EntryAdded)
    }
}
