package com.meteohealth

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.meteohealth.di.databaseModule
import com.meteohealth.di.networkModule
import com.meteohealth.di.repositoryModule
import com.meteohealth.di.viewModelModule
import com.meteohealth.notification.NotificationHelper
import com.meteohealth.worker.WeatherSyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(databaseModule, networkModule, repositoryModule, viewModelModule)
        }
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(KoinWorkerFactory())
                .build()
        )
        NotificationHelper.createChannel(this)
        scheduleWeatherSync()
    }

    private fun scheduleWeatherSync() {
        val request = PeriodicWorkRequestBuilder<WeatherSyncWorker>(3, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WeatherSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
