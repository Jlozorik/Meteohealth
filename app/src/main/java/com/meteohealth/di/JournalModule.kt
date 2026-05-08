package com.meteohealth.di

import com.meteohealth.data.repository.JournalRepositoryImpl
import com.meteohealth.domain.gateway.JournalGateway
import com.meteohealth.domain.usecase.AnalyseTriggersUseCase
import com.meteohealth.domain.usecase.AppendJournalEntryUseCase
import com.meteohealth.domain.usecase.ClearJournalUseCase
import com.meteohealth.domain.usecase.DeleteJournalEntryUseCase
import org.koin.dsl.module

val journalModule = module {
    single<JournalGateway> { JournalRepositoryImpl(get<com.meteohealth.data.storage.AppDatabase>().journalDao()) }
    factory { AppendJournalEntryUseCase(get()) }
    factory { DeleteJournalEntryUseCase(get()) }
    factory { ClearJournalUseCase(get()) }
    factory { AnalyseTriggersUseCase(get(), get(), get()) }
}
