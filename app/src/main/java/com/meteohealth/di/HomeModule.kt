package com.meteohealth.di

import com.meteohealth.domain.usecase.ObserveHomeUseCase
import com.meteohealth.domain.usecase.RefreshNowUseCase
import com.meteohealth.domain.wellbeing.WellbeingPipeline
import org.koin.dsl.module

val homeModule = module {
    single { WellbeingPipeline.default() }
    factory { ObserveHomeUseCase(get(), get(), get(), get(), get()) }
    factory { RefreshNowUseCase(get(), get(), get()) }
}
