package com.meteohealth.domain.gateway

import com.meteohealth.domain.model.Recommendation
import com.meteohealth.domain.model.RiskLevel

interface RecommendationGateway {
    fun get(risk: RiskLevel): List<Recommendation>
}
