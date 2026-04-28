package com.meteohealth.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.meteohealth.domain.model.PressureUnit
import com.meteohealth.domain.model.UserProfile
import com.meteohealth.domain.repository.DiaryRepository
import com.meteohealth.domain.repository.UserProfileRepository
import com.meteohealth.worker.WeatherSyncWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SettingsViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val diaryRepository: DiaryRepository,
    private val context: Context
) : ViewModel() {

    val profile: StateFlow<UserProfile> = userProfileRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfile())

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userProfileRepository.save(profile.value.copy(notificationsEnabled = enabled))
            val wm = WorkManager.getInstance(context)
            if (enabled) {
                val request = PeriodicWorkRequestBuilder<WeatherSyncWorker>(3, TimeUnit.HOURS)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
                wm.enqueueUniquePeriodicWork(
                    WeatherSyncWorker.WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            } else {
                wm.cancelUniqueWork(WeatherSyncWorker.WORK_NAME)
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userProfileRepository.save(profile.value.copy(isDarkTheme = enabled))
        }
    }

    fun setPressureUnit(unit: PressureUnit) {
        viewModelScope.launch {
            userProfileRepository.save(profile.value.copy(pressureUnit = unit))
        }
    }

    fun clearDiary() {
        viewModelScope.launch { diaryRepository.clearAll() }
    }
}
