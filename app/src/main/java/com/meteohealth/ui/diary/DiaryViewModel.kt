package com.meteohealth.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.model.DiaryEntry
import com.meteohealth.domain.model.WellbeingLevel
import com.meteohealth.domain.repository.DiaryRepository
import com.meteohealth.domain.repository.KpRepository
import com.meteohealth.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryViewModel(
    private val diaryRepository: DiaryRepository,
    private val weatherRepository: WeatherRepository,
    private val kpRepository: KpRepository
) : ViewModel() {

    val entries: StateFlow<List<DiaryEntry>> = diaryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addEntry(
        wellbeingLevel: WellbeingLevel,
        symptoms: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            val weather = weatherRepository.observeCurrentWeather().first()
            val kp = kpRepository.observeLatestKp().first()
            diaryRepository.save(
                DiaryEntry(
                    timestamp = System.currentTimeMillis(),
                    wellbeingLevel = wellbeingLevel,
                    symptoms = symptoms.joinToString(","),
                    notes = notes.trim(),
                    temperatureCelsius = weather?.temperatureCelsius,
                    pressureHpa = weather?.pressureHpa,
                    kpIndex = kp
                )
            )
        }
    }

    fun delete(entry: DiaryEntry) {
        viewModelScope.launch { diaryRepository.delete(entry) }
    }
}
