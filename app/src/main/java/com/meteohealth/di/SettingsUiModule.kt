package com.meteohealth.di

import com.meteohealth.feature.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsUiModule = module {
    viewModel { SettingsViewModel(get(), get(), get()) }
}
