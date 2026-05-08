package com.meteohealth.data.network

import com.meteohealth.data.network.http.HttpClientFactory
import com.meteohealth.data.network.service.OpenWeatherService
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenWeatherServiceTest {

    private val mockCurrent = """
        {"dt":1715163600,"main":{"temp":12.3,"pressure":748.0,"humidity":64},
         "wind":{"speed":4.1},"weather":[{"description":"облачно","icon":"02d"}],"name":"Москва"}
    """.trimIndent()

    private val mockForecast = """
        {"list":[{"dt":1715167200,"main":{"temp":11.0,"pressure":747.0,"humidity":66},
         "wind":{"speed":3.8},"weather":[{"description":"дождь","icon":"10d"}]}]}
    """.trimIndent()

    private fun makeService(responseBody: String): OpenWeatherService {
        val engine = MockEngine { respond(responseBody, HttpStatusCode.OK,
            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())) }
        return OpenWeatherService(HttpClientFactory.owm(engine, "test-key"))
    }

    @Test fun current_parses_temperature() = runTest {
        val service = makeService(mockCurrent)
        val result = service.current(55.75, 37.62)
        assertEquals(12.3, result.main.temp, 0.01)
    }

    @Test fun current_parses_city_name() = runTest {
        val service = makeService(mockCurrent)
        assertEquals("Москва", service.current(55.75, 37.62).name)
    }

    @Test fun forecast_parses_list_size() = runTest {
        val service = makeService(mockForecast)
        assertEquals(1, service.forecast(55.75, 37.62).list.size)
    }
}
