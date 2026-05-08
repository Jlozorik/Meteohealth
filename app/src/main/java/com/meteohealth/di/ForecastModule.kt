package com.meteohealth.di

import com.meteohealth.feature.forecast.ForecastViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val forecastModule = module {
    viewModel { ForecastViewModel(get(), get()) }
}
