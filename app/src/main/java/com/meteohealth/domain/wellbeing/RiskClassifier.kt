package com.meteohealth.domain.wellbeing

import com.meteohealth.domain.model.RiskLevel

object RiskClassifier {
    fun classify(score: Int): RiskLevel = when {
        score >= 80 -> RiskLevel.CALM
        score >= 60 -> RiskLevel.WATCH
        score >= 40 -> RiskLevel.ALERT
        else        -> RiskLevel.HIGH
    }
}
