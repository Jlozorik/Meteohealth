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
    operator fun invoke(): Flow<ForecastFeed> {
        // WeatherDao хранит hourBucketEpoch = unixSeconds / 3600
        // передаём текущий час-эпоху, чтобы получить сегодня и будущее
        val currentHourEpoch = System.currentTimeMillis() / 3_600_000L
        return combine(
            weatherGateway.observeHistory(currentHourEpoch),
            kpGateway.observeHistory(0L),
        ) { hours, kp -> ForecastFeed(hours, kp) }
    }
}
