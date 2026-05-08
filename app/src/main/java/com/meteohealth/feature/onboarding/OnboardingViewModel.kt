package com.meteohealth.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.model.Profile
import com.meteohealth.domain.usecase.SaveProfileUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val saveProfile: SaveProfileUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun dispatch(intent: OnboardingIntent) {
        val (newState, effects) = OnboardingReducer.reduce(_state.value, intent)
        _state.value = newState
        effects.forEach { effect ->
            viewModelScope.launch {
                if (effect is OnboardingEffect.Finished) {
                    runCatching {
                        saveProfile(Profile(
                            name = newState.name,
                            age = newState.age.toIntOrNull() ?: 0,
                            sensitivity = newState.sensitivity,
                            healthConditions = newState.conditions.toList(),
                            city = newState.city,
                            lat = newState.lat,
                            lon = newState.lon,
                        ))
                    }
                }
                _effects.send(effect)
            }
        }
    }
}
