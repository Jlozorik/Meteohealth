package com.meteohealth.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.usecase.ClearJournalUseCase
import com.meteohealth.domain.usecase.ObserveProfileUseCase
import com.meteohealth.domain.usecase.SaveProfileUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val clearJournal: ClearJournalUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeProfile().filterNotNull().collect { profile ->
                dispatch(SettingsIntent.ProfileArrived(profile))
            }
        }
    }

    fun dispatch(intent: SettingsIntent) {
        val (newState, effects) = SettingsReducer.reduce(_state.value, intent)
        _state.value = newState
        effects.forEach { viewModelScope.launch { _effects.send(it) } }

        when (intent) {
            is SettingsIntent.NotifToggled,
            is SettingsIntent.PressureUnitChanged,
            is SettingsIntent.CitySelected,
            is SettingsIntent.SensitivityChanged -> viewModelScope.launch {
                runCatching { saveProfile(newState.profile) }
            }
            is SettingsIntent.ConfirmClear -> viewModelScope.launch {
                runCatching { clearJournal() }
            }
            else -> Unit
        }
    }
}
