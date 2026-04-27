package com.meteohealth.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.model.ForecastDay
import com.meteohealth.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForecastUiState(
    val days: List<ForecastDay> = emptyList(),
    val isLoading: Boolean = true,
    val error: Boolean = false
)

class ForecastViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    val uiState: StateFlow<ForecastUiState> get() = _uiState
    private val _uiState = MutableStateFlow(ForecastUiState())

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = false) }
            val days = weatherRepository.getForecast(55.75, 37.62)
            _uiState.update {
                it.copy(days = days, isLoading = false, error = days.isEmpty())
            }
        }
    }
}
