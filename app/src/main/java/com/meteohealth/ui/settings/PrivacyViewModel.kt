package com.meteohealth.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.repository.DiaryRepository
import com.meteohealth.domain.repository.UserProfileRepository
import kotlinx.coroutines.launch

class PrivacyViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    fun wipeAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            diaryRepository.clearAll()
            userProfileRepository.reset()
            onDone()
        }
    }
}
