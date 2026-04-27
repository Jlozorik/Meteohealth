package com.meteohealth.di

import com.meteohealth.ui.MainViewModel
import com.meteohealth.ui.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { OnboardingViewModel(get()) }
}
