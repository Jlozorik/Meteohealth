package com.meteohealth.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.TriggerAnalyzer
import com.meteohealth.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TriggersViewModel(
    diaryRepository: DiaryRepository
) : ViewModel() {

    val state = diaryRepository.observeAll()
        .map { entries ->
            TriggersUiState(
                entryCount = entries.size,
                results = TriggerAnalyzer.analyze(entries),
                hasEnoughData = entries.size >= 14
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TriggersUiState())
}

data class TriggersUiState(
    val entryCount: Int = 0,
    val results: List<TriggerAnalyzer.TriggerResult> = emptyList(),
    val hasEnoughData: Boolean = false
)
