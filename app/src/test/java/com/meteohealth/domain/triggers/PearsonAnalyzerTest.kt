package com.meteohealth.domain.triggers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PearsonAnalyzerTest {

    @Test fun perfect_positive_correlation() {
        val x = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val r = PearsonAnalyzer.analyze(x, x, "test").r
        assertEquals(1.0, r, 0.0001)
    }

    @Test fun perfect_negative_correlation() {
        val x = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val y = listOf(5.0, 4.0, 3.0, 2.0, 1.0)
        val r = PearsonAnalyzer.analyze(x, y, "test").r
        assertEquals(-1.0, r, 0.0001)
    }

    @Test fun no_correlation_constant_factor() {
        val x = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val y = listOf(3.0, 3.0, 3.0, 3.0, 3.0)
        val r = PearsonAnalyzer.analyze(x, y, "test").r
        assertEquals(0.0, r, 0.0001)
    }

    @Test fun single_sample_returns_zero() {
        val result = PearsonAnalyzer.analyze(listOf(1.0), listOf(1.0), "test")
        assertEquals(0.0, result.r, 0.0)
    }

    @Test fun sample_count_correct() {
        val x = List(20) { it.toDouble() }
        val result = PearsonAnalyzer.analyze(x, x, "test")
        assertEquals(20, result.sampleCount)
    }

    @Test fun strength_strong_when_abs_r_above_0_5() =
        assertEquals(CorrelationStrength.STRONG, PearsonAnalyzer.strength(0.7))

    @Test fun strength_moderate_when_0_3_to_0_5() =
        assertEquals(CorrelationStrength.MODERATE, PearsonAnalyzer.strength(0.4))

    @Test fun strength_weak_when_0_1_to_0_3() =
        assertEquals(CorrelationStrength.WEAK, PearsonAnalyzer.strength(0.2))

    @Test fun strength_none_when_below_0_1() =
        assertEquals(CorrelationStrength.NONE, PearsonAnalyzer.strength(0.05))

    @Test fun strength_works_for_negative_r() =
        assertEquals(CorrelationStrength.STRONG, PearsonAnalyzer.strength(-0.8))

    @Test fun real_data_pressure_headache() {
        val levels = listOf(3.0, 4.0, 2.0, 5.0, 3.0, 4.0, 2.0)
        val pressure = listOf(760.0, 755.0, 765.0, 750.0, 758.0, 753.0, 768.0)
        val result = PearsonAnalyzer.analyze(levels, pressure, "pressure")
        assertTrue(result.r < 0)
    }
}
