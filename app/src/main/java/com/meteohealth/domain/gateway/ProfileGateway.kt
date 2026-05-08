package com.meteohealth.domain.gateway

import com.meteohealth.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileGateway {
    fun observe(): Flow<Profile?>
    suspend fun save(profile: Profile)
}
