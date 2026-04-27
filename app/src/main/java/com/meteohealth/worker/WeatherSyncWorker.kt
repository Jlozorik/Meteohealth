package com.meteohealth.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meteohealth.domain.WellbeingCalculator
import com.meteohealth.domain.repository.KpRepository
import com.meteohealth.domain.repository.UserProfileRepository
import com.meteohealth.domain.repository.WeatherRepository
import com.meteohealth.notification.NotificationHelper
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val weatherRepository: WeatherRepository by inject()
    private val kpRepository: KpRepository by inject()
    private val userProfileRepository: UserProfileRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            weatherRepository.refreshWeather(55.75, 37.62)
            kpRepository.refreshKp()

            val weather = weatherRepository.observeCurrentWeather().first()
            val kp = kpRepository.observeLatestKp().first()
            val profile = userProfileRepository.observe().first()

            if (weather != null && profile.notificationsEnabled) {
                val history = weatherRepository.getHistoricalPressure(6)
                val pressureDelta = if (history.size >= 2) {
                    history.last().second - history.first().second
                } else 0f

                val index = WellbeingCalculator.calculate(
                    pressureDelta6h = pressureDelta,
                    kpIndex = kp ?: 0f,
                    tempDelta24h = 0f,
                    humidity = weather.humidity,
                    profile = profile
                )

                if (index < profile.notificationThreshold) {
                    NotificationHelper.showWellbeingAlert(
                        applicationContext,
                        wellbeingIndex = index,
                        description = weather.weatherDescription
                    )
                }
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "weather_sync"
    }
}
