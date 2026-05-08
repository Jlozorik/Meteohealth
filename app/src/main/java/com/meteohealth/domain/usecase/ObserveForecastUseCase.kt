package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.KpGateway
import com.meteohealth.domain.gateway.WeatherGateway
import com.meteohealth.domain.model.KpSample
import com.meteohealth.domain.model.WeatherHour
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class ForecastFeed(
    val hours: List<WeatherHour>,
    val kpSamples: List<KpSample>,
)

class ObserveForecastUseCase(
    private val weatherGateway: WeatherGateway,
    private val kpGateway: KpGateway,
) {
    operator fun invoke(): Flow<ForecastFeed> = combine(
        weatherGateway.observeHistory(System.currentTimeMillis() / 1000),
        kpGateway.observeHistory(System.currentTimeMillis() / 1000),
    ) { hours, kp -> ForecastFeed(hours, kp) }
}
