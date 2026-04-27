package com.meteohealth.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(
    @SerialName("dt") val timestamp: Long,
    @SerialName("name") val cityName: String,
    @SerialName("main") val main: MainDto,
    @SerialName("weather") val weather: List<WeatherConditionDto>,
    @SerialName("wind") val wind: WindDto
)

@Serializable
data class MainDto(
    @SerialName("temp") val tempKelvin: Float,
    @SerialName("pressure") val pressureHpa: Float,
    @SerialName("humidity") val humidity: Int
)

@Serializable
data class WeatherConditionDto(
    @SerialName("description") val description: String,
    @SerialName("icon") val icon: String
)

@Serializable
data class WindDto(
    @SerialName("speed") val speedMs: Float
)
