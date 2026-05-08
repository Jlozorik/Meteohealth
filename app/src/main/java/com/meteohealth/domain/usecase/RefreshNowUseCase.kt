package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.KpGateway
import com.meteohealth.domain.gateway.ProfileGateway
import com.meteohealth.domain.gateway.WeatherGateway
import com.meteohealth.domain.model.Profile
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

class RefreshNowUseCase(
    private val weatherGateway: WeatherGateway,
    private val kpGateway: KpGateway,
    private val profileGateway: ProfileGateway,
) {
    suspend operator fun invoke() = coroutineScope {
        val profile = profileGateway.observe().first() ?: Profile()
        val weatherJob = async { weatherGateway.refresh(profile.lat, profile.lon) }
        val kpJob = async { kpGateway.refresh() }
        weatherJob.await()
        kpJob.await()
    }
}
