package com.meteohealth.domain.wellbeing

import com.meteohealth.domain.model.Profile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WellbeingPipelineTest {

    private val pipeline = WellbeingPipeline.default()
    private val calm = WellbeingInput(0.0, 1.0, 0.0, 50.0, Profile())

    @Test fun perfect_conditions_score_100() {
        assertEquals(100, pipeline.compute(calm).score)
    }

    @Test fun score_never_below_zero() {
        val worst = WellbeingInput(100.0, 10.0, 50.0, 100.0, Profile(sensitivity = 5))
        assertTrue(pipeline.compute(worst).score >= 0)
    }

    @Test fun breakdown_contains_all_keys() {
        val result = pipeline.compute(calm)
        val keys = result.breakdown.keys
        assertTrue("pressure" in keys)
        assertTrue("kp" in keys)
        assertTrue("temp" in keys)
        assertTrue("humidity" in keys)
        assertTrue("personal" in keys)
    }

    @Test fun score_decreases_with_high_kp() {
        val lowKp = pipeline.compute(WellbeingInput(0.0, 2.0, 0.0, 50.0, Profile()))
        val highKp = pipeline.compute(WellbeingInput(0.0, 7.0, 0.0, 50.0, Profile()))
        assertTrue(highKp.score < lowKp.score)
    }

    @Test fun empty_pipeline_returns_full() {
        val empty = WellbeingPipeline(emptyList())
        assertEquals(WellbeingResult.Full, empty.compute(calm))
    }
}
