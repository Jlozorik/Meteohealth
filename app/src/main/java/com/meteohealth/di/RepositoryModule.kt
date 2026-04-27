package com.meteohealth.di

import com.meteohealth.BuildConfig
import com.meteohealth.data.repository.DiaryRepositoryImpl
import com.meteohealth.data.repository.FakeWeatherRepository
import com.meteohealth.data.repository.KpRepositoryImpl
import com.meteohealth.data.repository.RecommendationsRepository
import com.meteohealth.data.repository.UserProfileRepositoryImpl
import com.meteohealth.data.repository.WeatherRepositoryImpl
import com.meteohealth.domain.repository.DiaryRepository
import com.meteohealth.domain.repository.KpRepository
import com.meteohealth.domain.repository.UserProfileRepository
import com.meteohealth.domain.repository.WeatherRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single<WeatherRepository> {
        if (BuildConfig.DEBUG && BuildConfig.OPEN_WEATHER_API_KEY.isEmpty()) {
            FakeWeatherRepository()
        } else {
            WeatherRepositoryImpl(get(), get())
        }
    }
    single<KpRepository> { KpRepositoryImpl(get(), get()) }
    single<UserProfileRepository> { UserProfileRepositoryImpl(get()) }
    single<DiaryRepository> { DiaryRepositoryImpl(get()) }
    single { RecommendationsRepository(androidContext()) }
}
