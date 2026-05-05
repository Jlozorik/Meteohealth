package com.meteohealth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_history")
data class WeatherHistoryEntity(
    @PrimaryKey val timestamp: Long,
    val pressureHpa: Float,
    val temperatureCelsius: Float,
    val humidity: Int
)
