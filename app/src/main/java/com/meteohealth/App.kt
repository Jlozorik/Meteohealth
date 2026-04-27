package com.meteohealth

import android.app.Application
import com.meteohealth.di.databaseModule
import com.meteohealth.di.networkModule
import com.meteohealth.di.repositoryModule
import com.meteohealth.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(databaseModule, networkModule, repositoryModule, viewModelModule)
        }
    }
}
