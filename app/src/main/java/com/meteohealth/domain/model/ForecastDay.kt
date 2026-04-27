package com.meteohealth.domain.model

data class ForecastSlot(
    val timestamp: Long,
    val temperatureCelsius: Float,
    val pressureHpa: Float,
    val humidity: Int,
    val weatherDescription: String,
    val windSpeedMs: Float
)

data class ForecastDay(
    val dateMillis: Long,
    val slots: List<ForecastSlot>,
    val minTempCelsius: Float,
    val maxTempCelsius: Float,
    val wellbeingIndex: Int
)
