package com.meteohealth.domain.model

data class WeatherHour(
    val hourBucketEpoch: Long,
    val tempC: Double,
    val pressureHpa: Double,
    val humidity: Int,
    val windMps: Double,
    val description: String,
    val icon: String,
    val city: String,
)
