package com.meteohealth.domain

import com.meteohealth.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WellbeingCalculatorTest {

    private val emptyProfile = UserProfile()

    @Test
    fun `ideal conditions give 100`() {
        val result = WellbeingCalculator.calculate(
            pressureDelta6h = 0f,
            kpIndex = 0f,
            tempDelta24h = 0f,
            humidity = 50,
            profile = emptyProfile
        )
        assertEquals(100, result)
    }

    @Test
    fun `result is always in range 0 to 100`() {
        val result = WellbeingCalculator.calculate(
            pressureDelta6h = 100f,
            kpIndex = 9f,
            tempDelta24h = 50f,
            humidity = 100,
            profile = emptyProfile
        )
        assertTrue("Expected 0..100, got $result", result in 0..100)
    }

    @Test
    fun `max pressure penalty is 30`() {
        val result = WellbeingCalculator.calculate(
            pressureDelta6h = 100f,
            kpIndex = 0f,
            tempDelta24h = 0f,
            humidity = 50,
            profile = emptyProfile
        )
        assertEquals(70, result)
    }

    @Test
    fun `max kp penalty is 30`() {
        val result = WellbeingCalculator.calculate(
            pressureDelta6h = 0f,
            kpIndex = 9f,
            tempDelta24h = 0f,
            humidity = 50,
            profile = emptyProfile
        )
        assertEquals(70, result)
    }

    @Test
    fun `hypertension penalty applied when pressure delta large`() {
        val profile = UserProfile(hasHypertension = true)
        val withPenalty = WellbeingCalculator.calculate(
            pressureDelta6h = 10f,
            kpIndex = 0f,
            tempDelta24h = 0f,
            humidity = 50,
            profile = profile
        )
        val withoutPenalty = WellbeingCalculator.calculate(
            pressureDelta6h = 10f,
            kpIndex = 0f,
            tempDelta24h = 0f,
            humidity = 50,
            profile = emptyProfile
        )
        assertTrue("Hypertension penalty not applied", withPenalty < withoutPenalty)
    }

    @Test
    fun `migraine penalty applied when kp high`() {
        val profile = UserProfile(hasMigraines = true)
        val withPenalty = WellbeingCalculator.calculate(
            pressureDelta6h = 0f,
            kpIndex = 5f,
            tempDelta24h = 0f,
            humidity = 50,
            profile = profile
        )
        val withoutPenalty = WellbeingCalculator.calculate(
            pressureDelta6h = 0f,
            kpIndex = 5f,
            tempDelta24h = 0f,
            humidity = 50,
            profile = emptyProfile
        )
        assertTrue("Migraine penalty not applied", withPenalty < withoutPenalty)
    }
}
