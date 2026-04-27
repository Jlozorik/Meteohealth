package com.meteohealth.data.repository

import com.meteohealth.data.local.dao.WeatherCacheDao
import com.meteohealth.data.local.entity.WeatherCacheEntity
import com.meteohealth.data.remote.api.WeatherApi
import com.meteohealth.domain.model.WeatherSnapshot
import com.meteohealth.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeatherRepositoryImpl(
    private val api: WeatherApi,
    private val dao: WeatherCacheDao
) : WeatherRepository {

    override fun observeCurrentWeather(): Flow<WeatherSnapshot?> =
        dao.observe().map { it?.toDomain() }

    override suspend fun refreshWeather(lat: Double, lon: Double) {
        try {
            val dto = api.getCurrentWeather(lat, lon)
            dao.upsert(
                WeatherCacheEntity(
                    timestamp = dto.timestamp * 1000L,
                    temperatureCelsius = dto.main.tempKelvin,
                    pressureHpa = dto.main.pressureHpa,
                    humidity = dto.main.humidity,
                    weatherDescription = dto.weather.firstOrNull()?.description.orEmpty(),
                    weatherIcon = dto.weather.firstOrNull()?.icon.orEmpty(),
                    windSpeedMs = dto.wind.speedMs,
                    cityName = dto.cityName
                )
            )
        } catch (_: Exception) {
            // offline-first: ошибка сети — возвращаем кэш через Flow
        }
    }

    override suspend fun getHistoricalPressure(hoursBack: Int): List<Pair<Long, Float>> {
        val cached = dao.get() ?: return emptyList()
        return listOf(cached.timestamp to cached.pressureHpa)
    }

    private fun WeatherCacheEntity.toDomain() = WeatherSnapshot(
        timestamp = timestamp,
        temperatureCelsius = temperatureCelsius,
        pressureHpa = pressureHpa,
        humidity = humidity,
        weatherDescription = weatherDescription,
        weatherIcon = weatherIcon,
        windSpeedMs = windSpeedMs,
        cityName = cityName
    )
}
