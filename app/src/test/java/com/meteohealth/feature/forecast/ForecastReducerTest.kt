package com.meteohealth.feature.forecast

import com.meteohealth.domain.model.PressureUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForecastReducerTest {

    private val initial = ForecastState()

    private fun sampleDay(i: Int) = DayRow("0$i.05", 5.0, 15.0, 750.0, 3.0, "ровно", emptyList())

    @Test fun feed_arrived_clears_loading() {
        val (state, _) = ForecastReducer.reduce(initial.copy(isLoading = true),
            ForecastIntent.FeedArrived(listOf(sampleDay(1)), emptyList()))
        assertFalse(state.isLoading)
        assertEquals(1, state.days.size)
    }

    @Test fun toggle_day_flips_expanded() {
        val loaded = initial.copy(days = listOf(sampleDay(1), sampleDay(2)), isLoading = false)
        val (state, _) = ForecastReducer.reduce(loaded, ForecastIntent.ToggleDay(0))
        assertTrue(state.days[0].expanded)
        assertFalse(state.days[1].expanded)
    }

    @Test fun toggle_day_collapses_on_second_tap() {
        val expanded = initial.copy(days = listOf(sampleDay(1).copy(expanded = true)), isLoading = false)
        val (state, _) = ForecastReducer.reduce(expanded, ForecastIntent.ToggleDay(0))
        assertFalse(state.days[0].expanded)
    }

    @Test fun toggle_pressure_unit() {
        val (state, _) = ForecastReducer.reduce(initial,
            ForecastIntent.TogglePressureUnit(PressureUnit.MM_HG))
        assertEquals(PressureUnit.MM_HG, state.pressureUnit)
    }

    @Test fun error_sets_message() {
        val (state, _) = ForecastReducer.reduce(initial, ForecastIntent.ErrorOccurred("fail"))
        assertEquals("fail", state.error)
        assertFalse(state.isLoading)
    }
}
