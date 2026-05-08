package com.meteohealth.domain.gateway

import com.meteohealth.domain.model.WeatherHour
import kotlinx.coroutines.flow.Flow

interface WeatherGateway {
    fun observeLatest(): Flow<WeatherHour?>
    fun observeHistory(sinceEpoch: Long): Flow<List<WeatherHour>>
    suspend fun refresh(lat: Double, lon: Double)
}
