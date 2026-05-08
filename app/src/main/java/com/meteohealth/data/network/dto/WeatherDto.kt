package com.meteohealth.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(
    val dt: Long,
    val main: MainDto,
    val wind: WindDto,
    val weather: List<WeatherItemDto> = emptyList(),
    val name: String = "",
)

@Serializable
data class ForecastResponseDto(
    val list: List<ForecastItemDto>,
)

@Serializable
data class ForecastItemDto(
    val dt: Long,
    val main: MainDto,
    val wind: WindDto,
    val weather: List<WeatherItemDto> = emptyList(),
)

@Serializable
data class MainDto(
    val temp: Double,
    @SerialName("pressure") val pressureHpa: Double,
    val humidity: Int,
)

@Serializable
data class WindDto(val speed: Double)

@Serializable
data class WeatherItemDto(
    val description: String = "",
    val icon: String = "",
)
