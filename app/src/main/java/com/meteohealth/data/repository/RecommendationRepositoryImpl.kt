package com.meteohealth.data.repository

import android.content.Context
import com.meteohealth.domain.gateway.RecommendationGateway
import com.meteohealth.domain.model.Recommendation
import com.meteohealth.domain.model.RiskLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class RecommendationJson(
    val id: String,
    val risk: String,
    val text: String,
)

class RecommendationRepositoryImpl(context: Context) : RecommendationGateway {

    private val all: List<RecommendationJson> = runCatching {
        val raw = context.assets.open("recommendations.json").bufferedReader().readText()
        Json { ignoreUnknownKeys = true }.decodeFromString<List<RecommendationJson>>(raw)
    }.getOrDefault(emptyList())

    override fun get(risk: RiskLevel): List<Recommendation> =
        all.filter { it.risk.uppercase() == risk.name }
            .map { Recommendation(it.id, it.text) }
}
