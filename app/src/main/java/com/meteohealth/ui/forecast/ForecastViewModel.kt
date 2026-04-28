package com.meteohealth.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.WellbeingPredictor
import com.meteohealth.domain.model.ForecastDay
import com.meteohealth.domain.repository.DiaryRepository
import com.meteohealth.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForecastUiState(
    val days: List<ForecastDay> = emptyList(),
    val personalIndices: Map<Long, Int> = emptyMap(),
    val predictorTrained: Boolean = false,
    val isLoading: Boolean = true,
    val error: Boolean = false
)

class ForecastViewModel(
    private val weatherRepository: WeatherRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    val uiState: StateFlow<ForecastUiState> get() = _uiState
    private val _uiState = MutableStateFlow(ForecastUiState())

    private val predictor = WellbeingPredictor()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = false) }

            val entries = diaryRepository.observeAll().first()
            predictor.train(entries)

            val days = weatherRepository.getForecast(55.75, 37.62)

            val personalIndices = if (predictor.isTrained) {
                days.associate { day ->
                    val avgPressure = day.slots.map { it.pressureHpa }.average().toFloat()
                    val avgTemp = day.slots.map { it.temperatureCelsius }.average().toFloat()
                    day.dateMillis to predictor.predict(avgPressure, avgTemp)
                }
            } else emptyMap()

            _uiState.update {
                it.copy(
                    days = days,
                    personalIndices = personalIndices,
                    predictorTrained = predictor.isTrained,
                    isLoading = false,
                    error = days.isEmpty()
                )
            }
        }
    }
}
