package com.meteohealth.di

import com.meteohealth.ui.MainViewModel
import com.meteohealth.ui.dashboard.DashboardViewModel
import com.meteohealth.ui.diary.DiaryViewModel
import com.meteohealth.ui.diary.TriggersViewModel
import com.meteohealth.ui.forecast.ForecastViewModel
import com.meteohealth.ui.onboarding.OnboardingViewModel
import com.meteohealth.ui.recommendations.RecommendationsViewModel
import com.meteohealth.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { ForecastViewModel(get()) }
    viewModel { DiaryViewModel(get(), get(), get(), get()) }
    viewModel { TriggersViewModel(get()) }
    viewModel { RecommendationsViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), androidContext()) }
}
