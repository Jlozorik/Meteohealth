package com.meteohealth.domain.wellbeing

data class WellbeingResult(val score: Int, val breakdown: Map<String, Int>) {
    companion object {
        val Full = WellbeingResult(100, emptyMap())
    }
}
