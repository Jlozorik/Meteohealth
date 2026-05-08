package com.meteohealth.background

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.meteohealth.domain.usecase.RefreshNowUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class WeatherTickService : Service() {

    private val refreshNow: RefreshNowUseCase by inject()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NotificationCenter.NOTIF_SYNC_ID,
            NotificationCenter.buildSyncNotification(this),
        )
        scope.launch {
            runCatching { refreshNow() }
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
