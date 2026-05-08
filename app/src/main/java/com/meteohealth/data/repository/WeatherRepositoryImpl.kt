package com.meteohealth.data.repository

import com.meteohealth.data.network.service.OpenWeatherService
import com.meteohealth.data.storage.dao.WeatherDao
import com.meteohealth.data.storage.entity.WeatherHourEntity
import com.meteohealth.domain.gateway.WeatherGateway
import com.meteohealth.domain.model.WeatherHour
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeatherRepositoryImpl(
    private val weatherDao: WeatherDao,
    private val service: OpenWeatherService,
) : WeatherGateway {

    override fun observeLatest(): Flow<WeatherHour?> =
        weatherDao.observeLatest().map { it?.toDomain() }

    override fun observeHistory(sinceEpoch: Long): Flow<List<WeatherHour>> =
        weatherDao.observeHistory(sinceEpoch).map { list -> list.map { it.toDomain() } }

    override suspend fun refresh(lat: Double, lon: Double) {
        val current = service.current(lat, lon)
        val forecast = service.forecast(lat, lon)

        val entities = mutableListOf<WeatherHourEntity>()
        val currentBucket = current.dt / 3600
        entities += WeatherHourEntity(
            hourBucketEpoch = currentBucket,
            t = current.main.temp,
            p = current.main.pressureHpa,
            h = current.main.humidity,
            wind = current.wind.speed,
            descr = current.weather.firstOrNull()?.description ?: "",
            icon = current.weather.firstOrNull()?.icon ?: "",
            city = current.name,
        )
        forecast.list.forEach { item ->
            entities += WeatherHourEntity(
                hourBucketEpoch = item.dt / 3600,
                t = item.main.temp,
                p = item.main.pressureHpa,
                h = item.main.humidity,
                wind = item.wind.speed,
                descr = item.weather.firstOrNull()?.description ?: "",
                icon = item.weather.firstOrNull()?.icon ?: "",
                city = current.name,
            )
        }
        weatherDao.upsertAll(entities)

        val cutoff = (System.currentTimeMillis() / 1000) - 7 * 24 * 3600
        weatherDao.deleteOlderThan(cutoff / 3600)
    }

    private fun WeatherHourEntity.toDomain() = WeatherHour(
        hourBucketEpoch = hourBucketEpoch,
        tempC = t,
        pressureHpa = p,
        humidity = h,
        windMps = wind,
        description = descr,
        icon = icon,
        city = city,
    )
}
