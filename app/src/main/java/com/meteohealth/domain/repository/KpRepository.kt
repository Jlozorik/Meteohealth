package com.meteohealth.domain.repository

import kotlinx.coroutines.flow.Flow

interface KpRepository {
    fun observeLatestKp(): Flow<Float?>
    suspend fun refreshKp()
}
