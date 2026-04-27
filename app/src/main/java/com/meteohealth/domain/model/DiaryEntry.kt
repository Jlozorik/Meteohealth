package com.meteohealth.domain.model

data class DiaryEntry(
    val id: Long = 0,
    val timestamp: Long,
    val wellbeingLevel: WellbeingLevel,
    val symptoms: String = "",
    val notes: String = "",
    val temperatureCelsius: Float? = null,
    val pressureHpa: Float? = null,
    val kpIndex: Float? = null
)
