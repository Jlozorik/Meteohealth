package com.meteohealth.data.network.service

import com.meteohealth.data.network.dto.KpEntryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/** Сервис NOAA SWPC — отдельный HttpClient без OWM defaultRequest. */
class NoaaSwpcService(private val client: HttpClient) {

    suspend fun kpIndex(): List<KpEntryDto> =
        client.get("https://services.swpc.noaa.gov/json/planetary_k_index_1m.json").body()
}
