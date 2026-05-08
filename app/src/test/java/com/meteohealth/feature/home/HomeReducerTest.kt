package com.meteohealth.feature.home

import com.meteohealth.domain.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeReducerTest {

    private val initial = HomeState()

    @Test fun refresh_sets_loading() {
        val (state, effects) = HomeReducer.reduce(initial, HomeIntent.Refresh)
        assertTrue(state.isLoading)
        assertTrue(effects.isEmpty())
    }

    @Test fun feed_arrived_clears_loading() {
        val loading = initial.copy(isLoading = true)
        val intent = HomeIntent.FeedArrived(75, RiskLevel.WATCH, "Ровно.", null, null, 0.0, emptyList())
        val (state, _) = HomeReducer.reduce(loading, intent)
        assertFalse(state.isLoading)
        assertEquals(75, state.score)
        assertEquals("Ровно.", state.verdict)
    }

    @Test fun error_emits_effect() {
        val (state, effects) = HomeReducer.reduce(initial, HomeIntent.ErrorOccurred("упс"))
        assertFalse(state.isLoading)
        assertEquals("упс", state.error)
        assertEquals(1, effects.size)
        assertTrue(effects.first() is HomeEffect.ShowRefreshError)
    }

    @Test fun refresh_clears_error() {
        val withError = initial.copy(error = "old error")
        val (state, _) = HomeReducer.reduce(withError, HomeIntent.Refresh)
        assertNull(state.error)
    }
}
