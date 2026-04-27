package com.meteohealth.data.repository

import com.meteohealth.data.local.dao.WeatherCacheDao
import com.meteohealth.data.local.entity.WeatherCacheEntity
import com.meteohealth.data.remote.api.WeatherApi
import com.meteohealth.domain.WellbeingCalculator
import com.meteohealth.domain.model.ForecastDay
import com.meteohealth.domain.model.ForecastSlot
import com.meteohealth.domain.model.UserProfile
import com.meteohealth.domain.model.WeatherSnapshot
import com.meteohealth.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.TimeZone

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

    override suspend fun getForecast(lat: Double, lon: Double): List<ForecastDay> {
        return try {
            val dto = api.getForecast(lat, lon)
            val slots = dto.list.map { item ->
                ForecastSlot(
                    timestamp = item.timestamp * 1000L,
                    temperatureCelsius = item.main.tempKelvin,
                    pressureHpa = item.main.pressureHpa,
                    humidity = item.main.humidity,
                    weatherDescription = item.weather.firstOrNull()?.description.orEmpty(),
                    windSpeedMs = item.wind.speedMs
                )
            }
            groupSlotsByDay(slots)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun groupSlotsByDay(slots: List<ForecastSlot>): List<ForecastDay> {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        return slots.groupBy { slot ->
            cal.timeInMillis = slot.timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.map { (dayMillis, daySlots) ->
            val pressureDelta = if (daySlots.size > 1)
                daySlots.last().pressureHpa - daySlots.first().pressureHpa else 0f
            val tempDelta = if (daySlots.size > 1)
                daySlots.last().temperatureCelsius - daySlots.first().temperatureCelsius else 0f
            val avgHumidity = daySlots.map { it.humidity }.average().toInt()
            ForecastDay(
                dateMillis = dayMillis,
                slots = daySlots,
                minTempCelsius = daySlots.minOf { it.temperatureCelsius },
                maxTempCelsius = daySlots.maxOf { it.temperatureCelsius },
                wellbeingIndex = WellbeingCalculator.calculate(
                    pressureDelta6h = pressureDelta,
                    kpIndex = 0f,
                    tempDelta24h = tempDelta,
                    humidity = avgHumidity,
                    profile = UserProfile()
                )
            )
        }.sortedBy { it.dateMillis }
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
