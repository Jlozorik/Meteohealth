package com.meteohealth.feature.settings

import com.meteohealth.domain.model.PressureUnit
import com.meteohealth.domain.model.Profile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsReducerTest {

    private val initial = SettingsState()

    @Test fun toggle_section_opens_it() {
        val (state, _) = SettingsReducer.reduce(initial, SettingsIntent.ToggleSection("city"))
        assertEquals("city", state.expandedSection)
    }

    @Test fun toggle_same_section_closes_it() {
        val open = initial.copy(expandedSection = "city")
        val (state, _) = SettingsReducer.reduce(open, SettingsIntent.ToggleSection("city"))
        assertNull(state.expandedSection)
    }

    @Test fun notif_toggled_updates_prefs() {
        val p = Profile(notificationPrefs = mapOf("main" to true))
        val s = initial.copy(profile = p)
        val (state, _) = SettingsReducer.reduce(s, SettingsIntent.NotifToggled("main", false))
        assertFalse(state.profile.notificationPrefs["main"]!!)
    }

    @Test fun pressure_unit_changed() {
        val (state, _) = SettingsReducer.reduce(initial, SettingsIntent.PressureUnitChanged(PressureUnit.MM_HG))
        assertEquals(PressureUnit.MM_HG, state.profile.pressureUnit)
    }

    @Test fun confirm_clear_emits_effect() {
        val withConfirm = initial.copy(showClearConfirm = true)
        val (state, effects) = SettingsReducer.reduce(withConfirm, SettingsIntent.ConfirmClear)
        assertFalse(state.showClearConfirm)
        assertEquals(1, effects.size)
        assertTrue(effects.first() is SettingsEffect.JournalCleared)
    }

    @Test fun city_selected_closes_section_and_updates_profile() {
        val open = initial.copy(expandedSection = "city", cityQuery = "Москва")
        val (state, _) = SettingsReducer.reduce(open, SettingsIntent.CitySelected("Москва", 55.75, 37.62))
        assertEquals("Москва", state.profile.city)
        assertNull(state.expandedSection)
        assertEquals("", state.cityQuery)
    }
}
