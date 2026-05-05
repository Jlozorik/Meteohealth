package com.meteohealth.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.WellbeingCalculator
import com.meteohealth.domain.model.UserProfile
import com.meteohealth.domain.model.WeatherSnapshot
import com.meteohealth.domain.repository.KpRepository
import com.meteohealth.domain.repository.UserProfileRepository
import com.meteohealth.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val weather: WeatherSnapshot? = null,
    val kpIndex: Float? = null,
    val wellbeingIndex: Int? = null,
    val breakdown: WellbeingCalculator.Breakdown? = null,
    val severity: WellbeingCalculator.Severity = WellbeingCalculator.Severity.GREEN,
    val pressureDelta6h: Float = 0f,
    val tempDelta24h: Float = 0f,
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

class DashboardViewModel(
    private val weatherRepository: WeatherRepository,
    private val kpRepository: KpRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    private val _deltas = MutableStateFlow(0f to 0f)

    val uiState: StateFlow<DashboardUiState> = combine(
        weatherRepository.observeCurrentWeather(),
        kpRepository.observeLatestKp(),
        userProfileRepository.observe(),
        _isRefreshing,
        _deltas.asStateFlow()
    ) { weather, kp, profile, refreshing, deltas ->
        val (pressureDelta, tempDelta) = deltas
        val breakdown = if (weather != null) {
            WellbeingCalculator.breakdown(
                pressureDelta6h = pressureDelta,
                kpIndex = kp ?: 0f,
                tempDelta24h = tempDelta,
                humidity = weather.humidity,
                profile = profile
            )
        } else null
        val severity = if (weather != null) {
            WellbeingCalculator.classifySeverity(
                pressureDelta6h = pressureDelta,
                kpIndex = kp ?: 0f,
                tempDelta24h = tempDelta,
                humidity = weather.humidity
            )
        } else WellbeingCalculator.Severity.GREEN
        DashboardUiState(
            weather = weather,
            kpIndex = kp,
            wellbeingIndex = breakdown?.total,
            breakdown = breakdown,
            severity = severity,
            pressureDelta6h = pressureDelta,
            tempDelta24h = tempDelta,
            profile = profile,
            isLoading = false,
            isRefreshing = refreshing
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardUiState()
    )

    init {
        // Пересчитываем дельты при каждом обновлении кэша погоды.
        weatherRepository.observeCurrentWeather()
            .onEach { recomputeDeltas() }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val profile = userProfileRepository.observe().first()
            val lat = profile.latitude ?: 55.75
            val lon = profile.longitude ?: 37.62
            weatherRepository.refreshWeather(lat, lon)
            kpRepository.refreshKp()
            recomputeDeltas()
            _isRefreshing.value = false
        }
    }

    private suspend fun recomputeDeltas() {
        val pressureHistory = weatherRepository.getHistoricalPressure(6)
        val tempHistory = weatherRepository.getHistoricalTemperature(24)
        val pDelta = if (pressureHistory.size >= 2)
            pressureHistory.last().second - pressureHistory.first().second else 0f
        val tDelta = if (tempHistory.size >= 2)
            tempHistory.last().second - tempHistory.first().second else 0f
        _deltas.value = pDelta to tDelta
    }
}
