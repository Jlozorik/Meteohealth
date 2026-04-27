package com.meteohealth.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.WellbeingCalculator
import com.meteohealth.domain.model.UserProfile
import com.meteohealth.domain.model.WeatherSnapshot
import com.meteohealth.domain.repository.KpRepository
import com.meteohealth.domain.repository.UserProfileRepository
import com.meteohealth.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val weather: WeatherSnapshot? = null,
    val kpIndex: Float? = null,
    val wellbeingIndex: Int? = null,
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val weatherRepository: WeatherRepository,
    private val kpRepository: KpRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        weatherRepository.observeCurrentWeather(),
        kpRepository.observeLatestKp(),
        userProfileRepository.observe()
    ) { weather, kp, profile ->
        DashboardUiState(
            weather = weather,
            kpIndex = kp,
            wellbeingIndex = if (weather != null) {
                WellbeingCalculator.calculate(
                    pressureDelta6h = 0f,
                    kpIndex = kp ?: 0f,
                    tempDelta24h = 0f,
                    humidity = weather.humidity,
                    profile = profile
                )
            } else null,
            profile = profile,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardUiState()
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            // Москва по умолчанию; координаты будут настраиваться в Settings
            weatherRepository.refreshWeather(55.75, 37.62)
            kpRepository.refreshKp()
        }
    }
}
