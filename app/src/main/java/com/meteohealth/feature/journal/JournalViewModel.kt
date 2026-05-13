package com.meteohealth.feature.journal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meteohealth.domain.gateway.JournalGateway
import com.meteohealth.domain.model.JournalEntry
import com.meteohealth.domain.usecase.AnalyseTriggersUseCase
import com.meteohealth.domain.usecase.AppendJournalEntryUseCase
import com.meteohealth.domain.usecase.DeleteJournalEntryUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class JournalViewModel(
    private val journalGateway: JournalGateway,
    private val appendEntry: AppendJournalEntryUseCase,
    private val deleteEntry: DeleteJournalEntryUseCase,
    private val analyseTriggers: AnalyseTriggersUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(JournalState())
    val state: StateFlow<JournalState> = _state.asStateFlow()

    private val _effects = Channel<JournalEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            journalGateway.observeAll().collect { entries ->
                dispatch(JournalIntent.EntriesArrived(entries))
            }
        }
        viewModelScope.launch {
            analyseTriggers().collect { results ->
                dispatch(JournalIntent.TriggersArrived(results))
            }
        }
    }

    fun dispatch(intent: JournalIntent) {
        Log.d("meowmeow",intent.toString())
        val (newState, effects) = JournalReducer.reduce(_state.value, intent)
        _state.value = newState
        effects.forEach { viewModelScope.launch { _effects.send(it) } }

        when (intent) {
            is JournalIntent.SubmitEntry -> viewModelScope.launch {
                val s = _state.value
                runCatching {
                    appendEntry(JournalEntry(
                        ts = System.currentTimeMillis(),
                        level = s.draftLevel,
                        notes = s.draftNotes,
                        symptoms = s.draftSymptoms.toList(),
                    ))
                }
            }
            is JournalIntent.DeleteEntry -> viewModelScope.launch {
                runCatching { deleteEntry(intent.id) }
            }
            else -> Unit
        }
    }
}
