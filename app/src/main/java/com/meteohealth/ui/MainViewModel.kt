package com.meteohealth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.repository.UserProfileRepository
import com.meteohealth.ui.navigation.NavRoutes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    userProfileRepository: UserProfileRepository
) : ViewModel() {

    val startDestination = userProfileRepository.observe()
        .map { profile ->
            if (profile.onboardingCompleted) NavRoutes.DASHBOARD else NavRoutes.ONBOARDING
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
