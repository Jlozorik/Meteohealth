package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.ProfileGateway
import com.meteohealth.domain.model.Profile
import kotlinx.coroutines.flow.Flow

class ObserveProfileUseCase(private val profileGateway: ProfileGateway) {
    operator fun invoke(): Flow<Profile?> = profileGateway.observe()
}
