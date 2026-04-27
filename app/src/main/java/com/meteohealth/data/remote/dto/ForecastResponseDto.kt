package com.meteohealth.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponseDto(
    @SerialName("list") val list: List<ForecastItemDto>,
    @SerialName("city") val city: ForecastCityDto
)

@Serializable
data class ForecastItemDto(
    @SerialName("dt") val timestamp: Long,
    @SerialName("main") val main: MainDto,
    @SerialName("weather") val weather: List<WeatherConditionDto>,
    @SerialName("wind") val wind: WindDto
)

@Serializable
data class ForecastCityDto(
    @SerialName("name") val name: String
)
