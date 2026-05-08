package com.meteohealth.di

import com.meteohealth.data.network.service.OpenWeatherService
import com.meteohealth.data.repository.WeatherRepositoryImpl
import com.meteohealth.domain.gateway.WeatherGateway
import com.meteohealth.domain.usecase.ObserveForecastUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val weatherModule = module {
    single { OpenWeatherService(get(named("owm"))) }
    single<WeatherGateway> { WeatherRepositoryImpl(get<com.meteohealth.data.storage.AppDatabase>().weatherDao(), get()) }
    factory { ObserveForecastUseCase(get(), get()) }
}
