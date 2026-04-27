package com.meteohealth.data.repository

import android.content.Context
import com.meteohealth.domain.model.Recommendation
import com.meteohealth.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class RecommendationDto(
    val id: String,
    val triggerCondition: String,
    val title: String,
    val text: String,
    val targetGroups: List<String>
)

/** Читает советы из assets и фильтрует по активным триггерам и профилю пользователя. */
class RecommendationsRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private var cachedAll: List<Recommendation>? = null

    private suspend fun loadAll(): List<Recommendation> = withContext(Dispatchers.IO) {
        cachedAll ?: run {
            val raw = context.assets.open("recommendations.json")
                .bufferedReader().use { it.readText() }
            val dtos = json.decodeFromString<List<RecommendationDto>>(raw)
            dtos.map { Recommendation(it.id, it.triggerCondition, it.title, it.text, it.targetGroups) }
                .also { cachedAll = it }
        }
    }

    suspend fun getRecommendations(
        pressureHpa: Float?,
        kpIndex: Float?,
        humidity: Int?,
        temperatureCelsius: Float?,
        profile: UserProfile
    ): List<Recommendation> {
        val all = loadAll()
        val activeTriggers = buildSet<String> {
            add("any")
            if (pressureHpa != null) {
                if (pressureHpa < 1000f) add("pressure_low")
                if (pressureHpa > 1020f) add("pressure_high")
            }
            if (kpIndex != null && kpIndex > 3f) add("kp_high")
            if (humidity != null && humidity > 70) add("humidity_high")
            if (temperatureCelsius != null && temperatureCelsius < 5f) add("temp_cold")
        }
        val userGroups = buildSet<String> {
            add("default")
            if (profile.hasHypertension) add("hypertensive")
            if (profile.hasMigraines) add("migraines")
            if (profile.hasJointPain) add("joint")
            if (profile.hasRespiratoryIssues) add("respiratory")
        }
        return all.filter { rec ->
            rec.triggerCondition in activeTriggers &&
                rec.targetGroups.any { it in userGroups }
        }
    }
}
