package com.meteohealth.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meteohealth.data.local.dao.NotificationLogDao
import com.meteohealth.data.local.entity.NotificationLogEntity
import com.meteohealth.domain.WellbeingCalculator
import com.meteohealth.domain.model.UserProfile
import com.meteohealth.domain.repository.KpRepository
import com.meteohealth.domain.repository.UserProfileRepository
import com.meteohealth.domain.repository.WeatherRepository
import com.meteohealth.notification.NotificationHelper
import com.meteohealth.notification.WeatherEvent
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
    private val notificationLogDao: NotificationLogDao by inject()

    override suspend fun doWork(): Result {
        return try {
            val profile = userProfileRepository.observe().first()
            val lat = profile.latitude ?: 55.75
            val lon = profile.longitude ?: 37.62

            weatherRepository.refreshWeather(lat, lon)
            kpRepository.refreshKp()

            val weather = weatherRepository.observeCurrentWeather().first()
            val kp = kpRepository.observeLatestKp().first() ?: 0f

            if (weather != null && profile.notificationsEnabled) {
                val pressureHistory = weatherRepository.getHistoricalPressure(6)
                val tempHistory = weatherRepository.getHistoricalTemperature(24)
                val pressureDelta = if (pressureHistory.size >= 2)
                    pressureHistory.last().second - pressureHistory.first().second else 0f
                val tempDelta = if (tempHistory.size >= 2)
                    tempHistory.last().second - tempHistory.first().second else 0f

                val index = WellbeingCalculator.calculate(
                    pressureDelta6h = pressureDelta,
                    kpIndex = kp,
                    tempDelta24h = tempDelta,
                    humidity = weather.humidity,
                    profile = profile
                )

                val event = detectEvent(pressureDelta, kp, weather.temperatureCelsius)
                val allowed = isEventAllowed(event, profile)

                if (allowed && (event != WeatherEvent.GENERAL || index < profile.notificationThreshold)) {
                    NotificationHelper.showAlert(
                        context = applicationContext,
                        event = event,
                        weatherDescription = weather.weatherDescription,
                        wellbeingIndex = index
                    )
                    val template = NotificationHelper.template(event, weather.weatherDescription, index)
                    notificationLogDao.insert(
                        NotificationLogEntity(
                            timestamp = System.currentTimeMillis(),
                            title = template.title,
                            body = template.body,
                            wellbeingIndex = index,
                            eventType = event.name,
                            severity = template.severity.name
                        )
                    )
                }
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun detectEvent(pressureDelta: Float, kp: Float, temperature: Float): WeatherEvent = when {
        kp >= 6f -> WeatherEvent.GEOMAGNETIC_STORM
        pressureDelta <= -6f -> WeatherEvent.PRESSURE_DROP
        pressureDelta >= 6f -> WeatherEvent.PRESSURE_RISE
        temperature <= -15f -> WeatherEvent.FROST
        temperature >= 32f -> WeatherEvent.HEAT
        else -> WeatherEvent.GENERAL
    }

    private fun isEventAllowed(event: WeatherEvent, profile: UserProfile): Boolean = when (event) {
        WeatherEvent.PRESSURE_DROP, WeatherEvent.PRESSURE_RISE -> profile.notifyPressureJump
        WeatherEvent.GEOMAGNETIC_STORM -> profile.notifyGeomagneticStorm
        WeatherEvent.FROST -> profile.notifyFrost
        WeatherEvent.HEAT -> profile.notifyHeat
        WeatherEvent.GENERAL -> true
    }

    companion object {
        const val WORK_NAME = "weather_sync"
    }
}
