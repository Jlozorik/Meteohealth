package com.meteohealth.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.meteohealth.R

enum class WeatherEvent { PRESSURE_DROP, PRESSURE_RISE, GEOMAGNETIC_STORM, FROST, HEAT, GENERAL }

enum class NotificationSeverity { INFO, URGENT }

object NotificationHelper {

    const val CHANNEL_INFO = "wellbeing_info"
    const val CHANNEL_URGENT = "wellbeing_urgent"
    private const val NOTIFICATION_ID = 1001

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_INFO,
                "Самочувствие — информация",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Информационные уведомления о погоде и индексе благополучия"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_URGENT,
                "Самочувствие — экстренные",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Магнитные бури, резкие скачки давления, экстремальная температура"
                enableVibration(true)
            }
        )
    }

    fun showAlert(
        context: Context,
        event: WeatherEvent,
        weatherDescription: String,
        wellbeingIndex: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val template = template(event, weatherDescription, wellbeingIndex)
        val channel = if (template.severity == NotificationSeverity.URGENT) CHANNEL_URGENT else CHANNEL_INFO
        val priority = if (template.severity == NotificationSeverity.URGENT)
            NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(template.title)
            .setContentText(template.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(template.body))
            .setPriority(priority)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    data class Template(
        val title: String,
        val body: String,
        val severity: NotificationSeverity
    )

    fun template(event: WeatherEvent, weatherDescription: String, wellbeingIndex: Int): Template = when (event) {
        WeatherEvent.GEOMAGNETIC_STORM -> Template(
            title = "Магнитная буря",
            body = "Геомагнитная активность повышена. Индекс самочувствия: $wellbeingIndex/100. " +
                "Снизьте нагрузку, пейте больше воды.",
            severity = NotificationSeverity.URGENT
        )
        WeatherEvent.PRESSURE_DROP -> Template(
            title = "Резкое падение давления",
            body = "Атмосферное давление быстро снижается. Индекс: $wellbeingIndex/100. " +
                "Возможны головные боли и слабость.",
            severity = NotificationSeverity.URGENT
        )
        WeatherEvent.PRESSURE_RISE -> Template(
            title = "Резкий рост давления",
            body = "Атмосферное давление быстро растёт. Индекс: $wellbeingIndex/100. " +
                "Гипертоникам — следите за самочувствием.",
            severity = NotificationSeverity.URGENT
        )
        WeatherEvent.FROST -> Template(
            title = "Сильный мороз",
            body = "Низкая температура ($weatherDescription). Индекс: $wellbeingIndex/100. " +
                "Ограничьте время на улице, тепло одевайтесь.",
            severity = NotificationSeverity.URGENT
        )
        WeatherEvent.HEAT -> Template(
            title = "Сильная жара",
            body = "Высокая температура ($weatherDescription). Индекс: $wellbeingIndex/100. " +
                "Избегайте прямого солнца, пейте воду.",
            severity = NotificationSeverity.URGENT
        )
        WeatherEvent.GENERAL -> {
            val text = when {
                wellbeingIndex < 40 -> "Очень неблагоприятно ($weatherDescription). Берегите себя."
                wellbeingIndex < 60 -> "Неблагоприятные условия ($weatherDescription). Будьте осторожны."
                else -> "Умеренно неблагоприятные условия ($weatherDescription)."
            }
            Template(
                title = "Индекс благополучия: $wellbeingIndex",
                body = text,
                severity = NotificationSeverity.INFO
            )
        }
    }
}
