package com.meteohealth.di

import com.meteohealth.feature.journal.JournalViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val journalUiModule = module {
    viewModel { JournalViewModel(get(), get(), get(), get()) }
}
