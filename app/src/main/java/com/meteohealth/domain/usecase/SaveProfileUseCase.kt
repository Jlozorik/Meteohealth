package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.ProfileGateway
import com.meteohealth.domain.model.Profile

class SaveProfileUseCase(private val profileGateway: ProfileGateway) {
    suspend operator fun invoke(profile: Profile) = profileGateway.save(profile)
}
