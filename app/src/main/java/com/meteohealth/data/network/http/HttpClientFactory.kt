package com.meteohealth.data.network.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun owm(engine: HttpClientEngine, apiKey: String): HttpClient = HttpClient(engine) {
        defaultRequest {
            url("https://api.openweathermap.org/data/2.5/")
            url.parameters.append("appid", apiKey)
            url.parameters.append("units", "metric")
            url.parameters.append("lang", "ru")
        }
        install(ContentNegotiation) { json(jsonConfig) }
        install(Logging) { level = LogLevel.HEADERS }
    }

    fun noaa(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) { json(jsonConfig) }
        install(Logging) { level = LogLevel.HEADERS }
    }

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}
