package com.meteohealth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val id: Int = 1,
    val timestamp: Long,
    val temperatureCelsius: Float,
    val pressureHpa: Float,
    val humidity: Int,
    val weatherDescription: String,
    val weatherIcon: String,
    val windSpeedMs: Float,
    val cityName: String
)
