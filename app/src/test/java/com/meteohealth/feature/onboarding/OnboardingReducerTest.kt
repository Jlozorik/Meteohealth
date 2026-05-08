package com.meteohealth.feature.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingReducerTest {

    private val initial = OnboardingState()

    @Test fun next_advances_step() {
        val (state, _) = OnboardingReducer.reduce(initial, OnboardingIntent.Next)
        assertEquals(1, state.step)
    }

    @Test fun back_from_first_step_is_noop() {
        val (state, effects) = OnboardingReducer.reduce(initial, OnboardingIntent.Back)
        assertEquals(0, state.step)
        assertTrue(effects.isEmpty())
    }

    @Test fun back_goes_to_previous() {
        val step2 = initial.copy(step = 2)
        val (state, _) = OnboardingReducer.reduce(step2, OnboardingIntent.Back)
        assertEquals(1, state.step)
    }

    @Test fun next_on_last_step_emits_finished() {
        val lastStep = initial.copy(step = 3)
        val (state, effects) = OnboardingReducer.reduce(lastStep, OnboardingIntent.Next)
        assertEquals(3, state.step)
        assertEquals(1, effects.size)
        assertTrue(effects.first() is OnboardingEffect.Finished)
    }

    @Test fun condition_toggle_adds_and_removes() {
        val (s1, _) = OnboardingReducer.reduce(initial, OnboardingIntent.ConditionToggled("мигрень"))
        assertTrue("мигрень" in s1.conditions)
        val (s2, _) = OnboardingReducer.reduce(s1, OnboardingIntent.ConditionToggled("мигрень"))
        assertTrue("мигрень" !in s2.conditions)
    }

    @Test fun sensitivity_changed() {
        val (state, _) = OnboardingReducer.reduce(initial, OnboardingIntent.SensitivityChanged(5))
        assertEquals(5, state.sensitivity)
    }
}
