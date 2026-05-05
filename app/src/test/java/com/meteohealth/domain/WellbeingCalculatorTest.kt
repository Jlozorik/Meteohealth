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
    fun `max pressure penalty is 35`() {
        val result = WellbeingCalculator.calculate(
            pressureDelta6h = 100f,
            kpIndex = 0f,
            tempDelta24h = 0f,
            humidity = 50,
            profile = emptyProfile
        )
        assertEquals(65, result)
    }

    @Test
    fun `max kp penalty is 40`() {
        val result = WellbeingCalculator.calculate(
            pressureDelta6h = 0f,
            kpIndex = 9f,
            tempDelta24h = 0f,
            humidity = 50,
            profile = emptyProfile
        )
        assertEquals(60, result)
    }

    @Test
    fun `kp 4 puts index into yellow zone`() {
        val result = WellbeingCalculator.calculate(
            pressureDelta6h = 0f,
            kpIndex = 4f,
            tempDelta24h = 0f,
            humidity = 50,
            profile = emptyProfile
        )
        // (4 − 2) * 10 = 20 штрафа → 80 баллов
        assertEquals(80, result)
    }

    @Test
    fun `storm with pressure jump pushes index into red zone`() {
        val result = WellbeingCalculator.calculate(
            pressureDelta6h = 8f,
            kpIndex = 6f,
            tempDelta24h = 10f,
            humidity = 50,
            profile = emptyProfile
        )
        // pressure: 32, kp: 40, temp: (10-3)*3=21, humidity: 0 → 100 − 93 = 7
        assertTrue("Expected red zone (<40), got $result", result < 40)
    }

    @Test
    fun `severity is green when all factors normal`() {
        val severity = WellbeingCalculator.classifySeverity(
            pressureDelta6h = 1f,
            kpIndex = 2f,
            tempDelta24h = 2f,
            humidity = 60
        )
        assertEquals(WellbeingCalculator.Severity.GREEN, severity)
    }

    @Test
    fun `severity is yellow with one factor off`() {
        val severity = WellbeingCalculator.classifySeverity(
            pressureDelta6h = 1f,
            kpIndex = 4.5f,
            tempDelta24h = 2f,
            humidity = 60
        )
        assertEquals(WellbeingCalculator.Severity.YELLOW, severity)
    }

    @Test
    fun `severity is red with storm and pressure jump`() {
        val severity = WellbeingCalculator.classifySeverity(
            pressureDelta6h = 5f,
            kpIndex = 5f,
            tempDelta24h = 0f,
            humidity = 60
        )
        assertEquals(WellbeingCalculator.Severity.RED, severity)
    }

    @Test
    fun `severity is red when two factors deviate`() {
        val severity = WellbeingCalculator.classifySeverity(
            pressureDelta6h = 5f,
            kpIndex = 4f,
            tempDelta24h = 2f,
            humidity = 60
        )
        assertEquals(WellbeingCalculator.Severity.RED, severity)
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

    @Test
    fun `breakdown reports per-factor penalties`() {
        val br = WellbeingCalculator.breakdown(
            pressureDelta6h = 5f,
            kpIndex = 5f,
            tempDelta24h = 6f,
            humidity = 80,
            profile = emptyProfile
        )
        assertEquals(20, br.pressurePenalty)
        assertEquals(30, br.kpPenalty)
        assertEquals(9, br.temperaturePenalty)
        assertEquals(5, br.humidityPenalty)
        assertEquals(0, br.personalPenalty)
        assertEquals(36, br.total)
    }
}
