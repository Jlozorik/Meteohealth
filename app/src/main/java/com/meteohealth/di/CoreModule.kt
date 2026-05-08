package com.meteohealth.di

import android.content.Context
import androidx.room.Room
import com.meteohealth.BuildConfig
import com.meteohealth.data.network.http.HttpClientFactory
import com.meteohealth.data.network.http.MockInterceptor
import com.meteohealth.data.storage.AppDatabase
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()
    }
    single(qualifier = org.koin.core.qualifier.named("owm")) {
        val apiKey = BuildConfig.OPEN_WEATHER_API_KEY
        val engine = OkHttp.create {
            if (BuildConfig.DEBUG && apiKey.isEmpty()) addInterceptor(MockInterceptor())
        }
        HttpClientFactory.owm(engine, apiKey)
    }
    single(qualifier = org.koin.core.qualifier.named("noaa")) {
        val apiKey = BuildConfig.OPEN_WEATHER_API_KEY
        val engine = OkHttp.create {
            if (BuildConfig.DEBUG && apiKey.isEmpty()) addInterceptor(MockInterceptor())
        }
        HttpClientFactory.noaa(engine)
    }
    single { Dispatchers.IO }
}
