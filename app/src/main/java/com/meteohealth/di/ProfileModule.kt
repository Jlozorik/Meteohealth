package com.meteohealth.di

import com.meteohealth.data.repository.ProfileRepositoryImpl
import com.meteohealth.data.repository.RecommendationRepositoryImpl
import com.meteohealth.domain.gateway.ProfileGateway
import com.meteohealth.domain.gateway.RecommendationGateway
import com.meteohealth.domain.usecase.ObserveProfileUseCase
import com.meteohealth.domain.usecase.SaveProfileUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val profileModule = module {
    single<ProfileGateway> { ProfileRepositoryImpl(get<com.meteohealth.data.storage.AppDatabase>().profileDao()) }
    single<RecommendationGateway> { RecommendationRepositoryImpl(androidContext()) }
    factory { ObserveProfileUseCase(get()) }
    factory { SaveProfileUseCase(get()) }
}
