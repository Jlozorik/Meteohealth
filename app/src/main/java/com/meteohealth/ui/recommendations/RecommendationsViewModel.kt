package com.meteohealth.ui.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.data.repository.RecommendationsRepository
import com.meteohealth.domain.model.Recommendation
import com.meteohealth.domain.repository.KpRepository
import com.meteohealth.domain.repository.UserProfileRepository
import com.meteohealth.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RecommendationsUiState(
    val recommendations: List<Recommendation> = emptyList(),
    val isLoading: Boolean = true
)

class RecommendationsViewModel(
    weatherRepository: WeatherRepository,
    kpRepository: KpRepository,
    userProfileRepository: UserProfileRepository,
    private val recommendationsRepository: RecommendationsRepository
) : ViewModel() {

    val uiState: StateFlow<RecommendationsUiState> = combine(
        weatherRepository.observeCurrentWeather(),
        kpRepository.observeLatestKp(),
        userProfileRepository.observe()
    ) { weather, kp, profile ->
        val recs = recommendationsRepository.getRecommendations(
            pressureHpa = weather?.pressureHpa,
            kpIndex = kp,
            humidity = weather?.humidity,
            temperatureCelsius = weather?.temperatureCelsius,
            profile = profile
        )
        RecommendationsUiState(recommendations = recs, isLoading = false)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RecommendationsUiState()
    )
}
