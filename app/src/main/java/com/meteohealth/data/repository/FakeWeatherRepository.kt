package com.meteohealth.data.repository

import com.meteohealth.domain.WellbeingCalculator
import com.meteohealth.domain.model.ForecastDay
import com.meteohealth.domain.model.ForecastSlot
import com.meteohealth.domain.model.UserProfile
import com.meteohealth.domain.model.WeatherSnapshot
import com.meteohealth.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWeatherRepository : WeatherRepository {

    private val _weather = MutableStateFlow<WeatherSnapshot?>(fakeSnapshot())

    override fun observeCurrentWeather(): Flow<WeatherSnapshot?> = _weather

    override suspend fun refreshWeather(lat: Double, lon: Double) {
        _weather.value = fakeSnapshot()
    }

    override suspend fun getHistoricalPressure(hoursBack: Int): List<Pair<Long, Float>> {
        val now = System.currentTimeMillis()
        return List(hoursBack) { i ->
            val ts = now - (hoursBack - i) * 3600_000L
            ts to (1013f + (Math.random() * 4 - 2).toFloat())
        }
    }

    override suspend fun getForecast(lat: Double, lon: Double): List<ForecastDay> {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 3600_000L
        val temps = listOf(16f, 19f, 14f, 21f, 18f)
        return temps.mapIndexed { i, baseTemp ->
            val dayStart = now - (now % dayMs) + (i + 1) * dayMs
            val slots = List(4) { j ->
                ForecastSlot(
                    timestamp = dayStart + j * 6 * 3600_000L,
                    temperatureCelsius = baseTemp + j * 0.5f,
                    pressureHpa = 1013f + i - j * 0.3f,
                    humidity = 60 + i * 3,
                    weatherDescription = listOf("ясно", "облачно", "дождь", "переменная облачность", "ясно")[i],
                    windSpeedMs = 3f + i * 0.5f
                )
            }
            ForecastDay(
                dateMillis = dayStart,
                slots = slots,
                minTempCelsius = slots.minOf { it.temperatureCelsius },
                maxTempCelsius = slots.maxOf { it.temperatureCelsius },
                wellbeingIndex = WellbeingCalculator.calculate(
                    pressureDelta6h = (i - 2).toFloat(),
                    kpIndex = 0f,
                    tempDelta24h = i.toFloat(),
                    humidity = 60 + i * 3,
                    profile = UserProfile()
                )
            )
        }
    }

    private fun fakeSnapshot() = WeatherSnapshot(
        timestamp = System.currentTimeMillis(),
        temperatureCelsius = 18f,
        pressureHpa = 1013f,
        humidity = 65,
        weatherDescription = "переменная облачность",
        weatherIcon = "02d",
        windSpeedMs = 3.5f,
        cityName = "Москва"
    )
}
