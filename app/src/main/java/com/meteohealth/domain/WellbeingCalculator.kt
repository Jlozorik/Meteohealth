package com.meteohealth.domain

import com.meteohealth.domain.model.UserProfile
import kotlin.math.abs

/**
 * Вычисляет индекс самочувствия (0–100) по формуле из ТЗ.
 */
object WellbeingCalculator {

    fun calculate(
        pressureDelta6h: Float,
        kpIndex: Float,
        tempDelta24h: Float,
        humidity: Int,
        profile: UserProfile
    ): Int {
        var score = 100f
        score -= (abs(pressureDelta6h) * 4f).coerceIn(0f, 30f)
        score -= ((kpIndex - 3f) * 8f).coerceIn(0f, 30f)
        score -= ((abs(tempDelta24h) - 5f) * 2f).coerceIn(0f, 20f)
        score -= ((humidity - 70f) * 0.5f).coerceIn(0f, 10f)
        score -= personalPenalty(profile, kpIndex, pressureDelta6h)
        return score.toInt().coerceIn(0, 100)
    }

    private fun personalPenalty(
        profile: UserProfile,
        kpIndex: Float,
        pressureDelta6h: Float
    ): Float {
        var penalty = 0f
        if (profile.hasHypertension && abs(pressureDelta6h) > 5f) penalty += 10f
        if (profile.hasMigraines && kpIndex > 4f) penalty += 10f
        if (profile.hasJointPain && abs(pressureDelta6h) > 3f) penalty += 5f
        if (profile.hasRespiratoryIssues && kpIndex > 5f) penalty += 5f
        return penalty.coerceIn(0f, 20f)
    }
}
