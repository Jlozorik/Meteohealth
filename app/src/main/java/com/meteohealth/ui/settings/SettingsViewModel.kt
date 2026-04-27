package com.meteohealth.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.meteohealth.domain.model.UserProfile
import com.meteohealth.domain.repository.DiaryRepository
import com.meteohealth.domain.repository.UserProfileRepository
import com.meteohealth.worker.WeatherSyncWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
            if (!enabled) {
                WorkManager.getInstance(context)
                    .cancelUniqueWork(WeatherSyncWorker.WORK_NAME)
            }
        }
    }

    fun clearDiary() {
        viewModelScope.launch { diaryRepository.clearAll() }
    }
}
