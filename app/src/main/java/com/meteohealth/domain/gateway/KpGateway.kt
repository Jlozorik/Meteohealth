package com.meteohealth.domain.gateway

import com.meteohealth.domain.model.KpSample
import kotlinx.coroutines.flow.Flow

interface KpGateway {
    fun observeLatest(): Flow<KpSample?>
    fun observeHistory(sinceEpoch: Long): Flow<List<KpSample>>
    suspend fun refresh()
}
