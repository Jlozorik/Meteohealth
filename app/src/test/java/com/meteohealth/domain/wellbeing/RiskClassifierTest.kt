package com.meteohealth.domain.wellbeing

import com.meteohealth.domain.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class RiskClassifierTest {

    @Test fun score_100_is_calm() = assertEquals(RiskLevel.CALM, RiskClassifier.classify(100))
    @Test fun score_80_is_calm() = assertEquals(RiskLevel.CALM, RiskClassifier.classify(80))
    @Test fun score_79_is_watch() = assertEquals(RiskLevel.WATCH, RiskClassifier.classify(79))
    @Test fun score_60_is_watch() = assertEquals(RiskLevel.WATCH, RiskClassifier.classify(60))
    @Test fun score_59_is_alert() = assertEquals(RiskLevel.ALERT, RiskClassifier.classify(59))
    @Test fun score_40_is_alert() = assertEquals(RiskLevel.ALERT, RiskClassifier.classify(40))
    @Test fun score_39_is_high() = assertEquals(RiskLevel.HIGH, RiskClassifier.classify(39))
    @Test fun score_0_is_high() = assertEquals(RiskLevel.HIGH, RiskClassifier.classify(0))
}
