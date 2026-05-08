package com.meteohealth.feature.forecast

import com.meteohealth.domain.model.KpSample
import com.meteohealth.domain.model.PressureUnit
import com.meteohealth.domain.model.WeatherHour

data class DayRow(
    val dateLabel: String,
    val tempMin: Double,
    val tempMax: Double,
    val pressureHpa: Double,
    val kp: Double,
    val riskLabel: String,
    val hours: List<WeatherHour>,
    val expanded: Boolean = false,
)

data class ForecastState(
    val days: List<DayRow> = emptyList(),
    val kpSamples: List<KpSample> = emptyList(),
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface ForecastIntent {
    data class FeedArrived(
        val days: List<DayRow>,
        val kpSamples: List<KpSample>,
    ) : ForecastIntent
    data class ToggleDay(val index: Int) : ForecastIntent
    data class TogglePressureUnit(val unit: PressureUnit) : ForecastIntent
    data class ErrorOccurred(val message: String) : ForecastIntent
}

sealed interface ForecastEffect
