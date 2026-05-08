package com.meteohealth.domain.wellbeing

import com.meteohealth.domain.model.Profile

data class WellbeingInput(
    val pressureDelta6h: Double,
    val kpIndex: Double,
    val tempDelta24h: Double,
    val humidity: Double,
    val profile: Profile,
)
