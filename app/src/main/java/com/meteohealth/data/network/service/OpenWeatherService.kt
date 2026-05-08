package com.meteohealth.data.network.service

import com.meteohealth.data.network.dto.ForecastResponseDto
import com.meteohealth.data.network.dto.WeatherResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Сервис OpenWeatherMap — обычный класс, не Retrofit-интерфейс. */
class OpenWeatherService(private val client: HttpClient) {

    suspend fun current(lat: Double, lon: Double): WeatherResponseDto =
        client.get("weather") {
            parameter("lat", lat)
            parameter("lon", lon)
        }.body()

    suspend fun forecast(lat: Double, lon: Double): ForecastResponseDto =
        client.get("forecast") {
            parameter("lat", lat)
            parameter("lon", lon)
        }.body()
}
