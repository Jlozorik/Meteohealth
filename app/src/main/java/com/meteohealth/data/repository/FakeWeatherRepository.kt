package com.meteohealth.data.repository

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
