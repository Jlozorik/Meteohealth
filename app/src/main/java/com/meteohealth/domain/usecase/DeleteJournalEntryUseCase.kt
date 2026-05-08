package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.JournalGateway

class DeleteJournalEntryUseCase(private val journalGateway: JournalGateway) {
    suspend operator fun invoke(id: Long) = journalGateway.delete(id)
}
