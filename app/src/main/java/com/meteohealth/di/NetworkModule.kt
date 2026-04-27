package com.meteohealth.di

import com.meteohealth.BuildConfig
import com.meteohealth.data.remote.api.ApiKeyInterceptor
import com.meteohealth.data.remote.api.KpApi
import com.meteohealth.data.remote.api.WeatherApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit

private val json = Json { ignoreUnknownKeys = true }

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(BuildConfig.OPEN_WEATHER_API_KEY))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
                    )
                }
            }
            .build()
    }

    single<WeatherApi> {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WeatherApi::class.java)
    }

    single<KpApi> {
        Retrofit.Builder()
            .baseUrl("https://services.swpc.noaa.gov/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(KpApi::class.java)
    }
}
