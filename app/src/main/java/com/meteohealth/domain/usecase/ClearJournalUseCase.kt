package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.JournalGateway

class ClearJournalUseCase(private val journalGateway: JournalGateway) {
    suspend operator fun invoke() = journalGateway.deleteAll()
}
