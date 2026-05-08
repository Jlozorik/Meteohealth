package com.meteohealth.data.network.http

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/** OkHttp interceptor, используется когда DEBUG && apiKey.isEmpty(). */
class MockInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.toString()
        val body = when {
            url.contains("forecast") -> MOCK_FORECAST
            url.contains("weather") -> MOCK_CURRENT
            url.contains("planetary_k_index") -> MOCK_KP
            else -> "{}"
        }
        return Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }
}

private val MOCK_CURRENT = """
{
  "dt": 1715163600,
  "main": { "temp": 12.3, "pressure": 748.0, "humidity": 64 },
  "wind": { "speed": 4.1 },
  "weather": [{ "description": "переменная облачность", "icon": "02d" }],
  "name": "Москва"
}
""".trimIndent()

private val MOCK_FORECAST = """
{
  "list": [
    { "dt": 1715167200, "main": { "temp": 11.0, "pressure": 747.0, "humidity": 66 }, "wind": { "speed": 3.8 }, "weather": [{"description":"облачно","icon":"03d"}] },
    { "dt": 1715178000, "main": { "temp": 9.5,  "pressure": 745.0, "humidity": 70 }, "wind": { "speed": 4.5 }, "weather": [{"description":"дождь","icon":"10d"}] },
    { "dt": 1715188800, "main": { "temp": 8.0,  "pressure": 743.0, "humidity": 73 }, "wind": { "speed": 5.0 }, "weather": [{"description":"дождь","icon":"10n"}] },
    { "dt": 1715250000, "main": { "temp": 6.5,  "pressure": 742.0, "humidity": 75 }, "wind": { "speed": 4.2 }, "weather": [{"description":"облачно","icon":"04d"}] },
    { "dt": 1715336400, "main": { "temp": 10.0, "pressure": 750.0, "humidity": 60 }, "wind": { "speed": 3.0 }, "weather": [{"description":"ясно","icon":"01d"}] }
  ]
}
""".trimIndent()

private val MOCK_KP = """
[
  { "time_tag": "2026-05-08 12:00:00", "kp_index": 3.2 },
  { "time_tag": "2026-05-08 12:01:00", "kp_index": 3.3 },
  { "time_tag": "2026-05-08 12:02:00", "kp_index": 3.1 }
]
""".trimIndent()
