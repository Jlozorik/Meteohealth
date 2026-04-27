package com.meteohealth.domain.model

data class WeatherSnapshot(
    val timestamp: Long,
    val temperatureCelsius: Float,
    val pressureHpa: Float,
    val humidity: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val windSpeedMs: Float,
    val cityName: String
)
