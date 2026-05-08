package com.meteohealth.di

import com.meteohealth.data.network.service.NoaaSwpcService
import com.meteohealth.data.repository.KpRepositoryImpl
import com.meteohealth.domain.gateway.KpGateway
import org.koin.core.qualifier.named
import org.koin.dsl.module

val kpModule = module {
    single { NoaaSwpcService(get(named("noaa"))) }
    single<KpGateway> { KpRepositoryImpl(get<com.meteohealth.data.storage.AppDatabase>().kpDao(), get()) }
}
