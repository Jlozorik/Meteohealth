package com.meteohealth.di

import androidx.room.Room
import com.meteohealth.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "meteohealth.db"
        ).build()
    }
    single { get<AppDatabase>().userProfileDao() }
    single { get<AppDatabase>().weatherCacheDao() }
    single { get<AppDatabase>().diaryEntryDao() }
    single { get<AppDatabase>().kpCacheDao() }
    single { get<AppDatabase>().notificationLogDao() }
}
