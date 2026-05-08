package com.meteohealth.domain.gateway

import com.meteohealth.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalGateway {
    fun observeAll(): Flow<List<JournalEntry>>
    suspend fun append(entry: JournalEntry)
    suspend fun delete(id: Long)
    suspend fun deleteAll()
}
