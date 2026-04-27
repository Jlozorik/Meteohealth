package com.meteohealth.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.model.UserProfile
import com.meteohealth.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingState(
    val name: String = "",
    val hasHypertension: Boolean = false,
    val hasMigraines: Boolean = false,
    val hasJointPain: Boolean = false,
    val hasRespiratoryIssues: Boolean = false
)

class OnboardingViewModel(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onHypertensionToggle() = _state.update { it.copy(hasHypertension = !it.hasHypertension) }
    fun onMigrainesToggle() = _state.update { it.copy(hasMigraines = !it.hasMigraines) }
    fun onJointPainToggle() = _state.update { it.copy(hasJointPain = !it.hasJointPain) }
    fun onRespiratoryToggle() = _state.update { it.copy(hasRespiratoryIssues = !it.hasRespiratoryIssues) }

    fun finish(onDone: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            userProfileRepository.save(
                UserProfile(
                    name = s.name.trim(),
                    hasHypertension = s.hasHypertension,
                    hasMigraines = s.hasMigraines,
                    hasJointPain = s.hasJointPain,
                    hasRespiratoryIssues = s.hasRespiratoryIssues,
                    onboardingCompleted = true
                )
            )
            onDone()
        }
    }
}
