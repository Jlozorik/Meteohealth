package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.JournalGateway
import com.meteohealth.domain.model.JournalEntry

class AppendJournalEntryUseCase(private val journalGateway: JournalGateway) {
    suspend operator fun invoke(entry: JournalEntry) = journalGateway.append(entry)
}
