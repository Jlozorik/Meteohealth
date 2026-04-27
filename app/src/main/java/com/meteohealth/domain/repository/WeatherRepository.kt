package com.meteohealth.domain.repository

import com.meteohealth.domain.model.WeatherSnapshot
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun observeCurrentWeather(): Flow<WeatherSnapshot?>
    suspend fun refreshWeather(lat: Double, lon: Double)
    suspend fun getHistoricalPressure(hoursBack: Int = 6): List<Pair<Long, Float>>
}
