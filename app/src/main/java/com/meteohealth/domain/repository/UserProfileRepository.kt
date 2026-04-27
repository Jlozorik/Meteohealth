package com.meteohealth.domain.repository

import com.meteohealth.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observe(): Flow<UserProfile>
    suspend fun save(profile: UserProfile)
}
