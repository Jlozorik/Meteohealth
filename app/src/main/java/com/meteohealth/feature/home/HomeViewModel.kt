package com.meteohealth.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.model.RiskLevel
import com.meteohealth.domain.usecase.ObserveHomeUseCase
import com.meteohealth.domain.usecase.RefreshNowUseCase
import com.meteohealth.domain.wellbeing.RiskClassifier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val observeHome: ObserveHomeUseCase,
    private val refreshNow: RefreshNowUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeHome()
                .catch { e -> dispatch(HomeIntent.ErrorOccurred(e.message ?: "Ошибка")) }
                .collect { feed ->
                    dispatch(HomeIntent.FeedArrived(
                        score = feed.wellbeing.score,
                        riskLevel = RiskClassifier.classify(feed.wellbeing.score),
                        verdict = verdictFor(RiskClassifier.classify(feed.wellbeing.score)),
                        weather = feed.weather,
                        kp = feed.kp,
                        pressureDelta = feed.pressureDelta6h,
                        recommendations = feed.recommendations,
                    ))
                }
        }
    }

    fun dispatch(intent: HomeIntent) {
        val (newState, effects) = HomeReducer.reduce(_state.value, intent)
        _state.value = newState
        effects.forEach { viewModelScope.launch { _effects.send(it) } }
        if (intent is HomeIntent.Refresh) {
            viewModelScope.launch {
                runCatching { refreshNow() }
                    .onFailure { dispatch(HomeIntent.ErrorOccurred(it.message ?: "Ошибка")) }
            }
        }
    }

    private fun verdictFor(risk: RiskLevel) = when (risk) {
        RiskLevel.CALM  -> "Спокойно."
        RiskLevel.WATCH -> "Ровно."
        RiskLevel.ALERT -> "Следи за давлением."
        RiskLevel.HIGH  -> "Береги голову."
    }
}
