package com.meteohealth.feature.home

import com.meteohealth.domain.model.KpSample
import com.meteohealth.domain.model.Recommendation
import com.meteohealth.domain.model.RiskLevel
import com.meteohealth.domain.model.WeatherHour

data class HomeState(
    val score: Int = 0,
    val riskLevel: RiskLevel = RiskLevel.CALM,
    val verdict: String = "",
    val weather: WeatherHour? = null,
    val kp: KpSample? = null,
    val pressureDelta6h: Double = 0.0,
    val recommendations: List<Recommendation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface HomeIntent {
    data object Refresh : HomeIntent
    data class FeedArrived(
        val score: Int,
        val riskLevel: RiskLevel,
        val verdict: String,
        val weather: WeatherHour?,
        val kp: KpSample?,
        val pressureDelta: Double,
        val recommendations: List<Recommendation>,
    ) : HomeIntent
    data class ErrorOccurred(val message: String) : HomeIntent
}

sealed interface HomeEffect {
    data object ShowRefreshError : HomeEffect
}
