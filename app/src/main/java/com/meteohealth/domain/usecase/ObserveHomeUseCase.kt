package com.meteohealth.domain.usecase

import com.meteohealth.domain.gateway.KpGateway
import com.meteohealth.domain.gateway.ProfileGateway
import com.meteohealth.domain.gateway.RecommendationGateway
import com.meteohealth.domain.gateway.WeatherGateway
import com.meteohealth.domain.model.KpSample
import com.meteohealth.domain.model.Profile
import com.meteohealth.domain.model.Recommendation
import com.meteohealth.domain.model.WeatherHour
import com.meteohealth.domain.wellbeing.RiskClassifier
import com.meteohealth.domain.wellbeing.WellbeingInput
import com.meteohealth.domain.wellbeing.WellbeingPipeline
import com.meteohealth.domain.wellbeing.WellbeingResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class HomeFeed(
    val weather: WeatherHour?,
    val kp: KpSample?,
    val profile: Profile,
    val wellbeing: WellbeingResult,
    val pressureDelta6h: Double,
    val recommendations: List<Recommendation>,
)

class ObserveHomeUseCase(
    private val weatherGateway: WeatherGateway,
    private val kpGateway: KpGateway,
    private val profileGateway: ProfileGateway,
    private val recommendationGateway: RecommendationGateway,
    private val pipeline: WellbeingPipeline,
) {
    operator fun invoke(): Flow<HomeFeed> = combine(
        weatherGateway.observeLatest(),
        weatherGateway.observeHistory(System.currentTimeMillis() / 3_600_000L - 6),
        kpGateway.observeLatest(),
        profileGateway.observe(),
    ) { latest, history, kp, profile ->
        val p = profile ?: Profile()
        val pressureDelta = if (history.size >= 2) {
            (latest?.pressureHpa ?: 0.0) - history.first().pressureHpa
        } else 0.0
        val input = WellbeingInput(
            pressureDelta6h = pressureDelta,
            kpIndex = kp?.kp ?: 0.0,
            tempDelta24h = 0.0,
            humidity = latest?.humidity?.toDouble() ?: 50.0,
            profile = p,
        )
        val result = pipeline.compute(input)
        val risk = RiskClassifier.classify(result.score)
        HomeFeed(
            weather = latest,
            kp = kp,
            profile = p,
            wellbeing = result,
            pressureDelta6h = pressureDelta,
            recommendations = recommendationGateway.get(risk).take(3),
        )
    }
}
