package com.meteohealth.data.remote.api

import com.meteohealth.data.remote.dto.KpEntryDto
import retrofit2.http.GET

interface KpApi {
    // Возвращает JSON-массив объектов: [{"time_tag": "...", "kp_index": 1.33}, ...]
    @GET("json/planetary_k_index_1m.json")
    suspend fun getKpData(): List<KpEntryDto>
}
