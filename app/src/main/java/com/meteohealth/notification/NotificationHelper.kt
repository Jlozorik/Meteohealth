package com.meteohealth.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.meteohealth.R

object NotificationHelper {

    const val CHANNEL_ID = "wellbeing_channel"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Самочувствие",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о неблагоприятных погодных условиях"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showWellbeingAlert(context: Context, wellbeingIndex: Int, description: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val text = when {
            wellbeingIndex < 40 -> "Очень неблагоприятно ($description). Берегите себя."
            wellbeingIndex < 60 -> "Неблагоприятные условия ($description). Будьте осторожны."
            else -> "Умеренно неблагоприятные условия ($description)."
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Индекс благополучия: $wellbeingIndex")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }
}
