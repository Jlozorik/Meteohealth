package com.meteohealth

import android.app.Application
import com.meteohealth.background.NotificationCenter
import com.meteohealth.background.TickReceiver
import com.meteohealth.di.allModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(allModules)
        }
        NotificationCenter.createChannels(this)
        TickReceiver.schedule(this)
    }
}
