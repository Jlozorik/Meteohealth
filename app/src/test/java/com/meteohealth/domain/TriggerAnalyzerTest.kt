package com.meteohealth.domain

import com.meteohealth.domain.model.DiaryEntry
import com.meteohealth.domain.model.WellbeingLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TriggerAnalyzerTest {

    private fun entry(
        wellbeing: WellbeingLevel,
        pressure: Float? = null,
        temp: Float? = null,
        kp: Float? = null,
        id: Long = 0
    ) = DiaryEntry(
        id = id,
        timestamp = System.currentTimeMillis(),
        wellbeingLevel = wellbeing,
        pressureHpa = pressure,
        temperatureCelsius = temp,
        kpIndex = kp
    )

    @Test
    fun `returns empty when fewer than 14 entries`() {
        val entries = List(13) { entry(WellbeingLevel.GOOD, pressure = 1013f) }
        assertTrue(TriggerAnalyzer.analyze(entries).isEmpty())
    }

    @Test
    fun `perfect positive correlation equals 1`() {
        val entries = List(14) { i ->
            val level = WellbeingLevel.entries[i % 5]
            val score = when (level) {
                WellbeingLevel.GREAT -> 5f
                WellbeingLevel.GOOD -> 4f
                WellbeingLevel.FAIR -> 3f
                WellbeingLevel.POOR -> 2f
                WellbeingLevel.TERRIBLE -> 1f
            }
            entry(level, pressure = score * 10f, id = i.toLong())
        }
        val results = TriggerAnalyzer.analyze(entries)
        val pressureResult = results.find { it.factor == TriggerAnalyzer.Factor.PRESSURE }
        checkNotNull(pressureResult)
        assertEquals(1.0f, pressureResult.correlation, 0.01f)
    }

    @Test
    fun `uncorrelated data returns near zero`() {
        val wellbeings = listOf(
            WellbeingLevel.GREAT, WellbeingLevel.TERRIBLE, WellbeingLevel.GREAT,
            WellbeingLevel.TERRIBLE, WellbeingLevel.GREAT, WellbeingLevel.TERRIBLE,
            WellbeingLevel.GREAT, WellbeingLevel.TERRIBLE, WellbeingLevel.GREAT,
            WellbeingLevel.TERRIBLE, WellbeingLevel.GREAT, WellbeingLevel.TERRIBLE,
            WellbeingLevel.GREAT, WellbeingLevel.TERRIBLE
        )
        val pressures = listOf(
            1000f, 1000f, 1010f, 1010f, 1020f, 1020f, 1030f, 1030f,
            1000f, 1010f, 1020f, 1030f, 1000f, 1010f
        )
        val entries = wellbeings.mapIndexed { i, w ->
            entry(w, pressure = pressures[i], id = i.toLong())
        }
        val results = TriggerAnalyzer.analyze(entries)
        val pressureResult = results.find { it.factor == TriggerAnalyzer.Factor.PRESSURE }
        checkNotNull(pressureResult)
        assertTrue("Expected near zero, got ${pressureResult.correlation}",
            kotlin.math.abs(pressureResult.correlation) < 0.3f)
    }

    @Test
    fun `factor omitted when fewer than 5 data points`() {
        val entries = List(14) { i ->
            if (i < 3) entry(WellbeingLevel.GOOD, kp = 2f, id = i.toLong())
            else entry(WellbeingLevel.GOOD, id = i.toLong())
        }
        val results = TriggerAnalyzer.analyze(entries)
        assertTrue(results.none { it.factor == TriggerAnalyzer.Factor.KP_INDEX })
    }
}
