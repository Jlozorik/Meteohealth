package com.meteohealth.data.remote.api

import retrofit2.http.GET

interface KpApi {
    // Возвращает JSON-массив массивов: [[timestamp, kp_index], ...]
    @GET("json/planetary_k_index_1m.json")
    suspend fun getKpData(): List<List<String>>
}
