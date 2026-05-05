package com.meteohealth.domain

import com.meteohealth.domain.model.Sensitivity
import com.meteohealth.domain.model.UserProfile
import kotlin.math.abs

/**
 * Вычисляет индекс самочувствия (0–100) и общий уровень риска по факторам погоды.
 *
 * Веса подобраны так, чтобы геомагнитная буря или скачок давления реально опускали
 * индекс ниже 60, а сочетание факторов уверенно загоняло в красную зону.
 */
object WellbeingCalculator {

    enum class Severity { GREEN, YELLOW, RED }

    /**
     * Вклад каждого фактора (для подписей в UI). Положительные значения —
     * сколько баллов фактор отнимает у максимума 100.
     */
    data class Breakdown(
        val pressurePenalty: Int,
        val kpPenalty: Int,
        val temperaturePenalty: Int,
        val humidityPenalty: Int,
        val personalPenalty: Int,
        val total: Int
    )

    fun calculate(
        pressureDelta6h: Float,
        kpIndex: Float,
        tempDelta24h: Float,
        humidity: Int,
        profile: UserProfile
    ): Int = breakdown(pressureDelta6h, kpIndex, tempDelta24h, humidity, profile).total

    fun breakdown(
        pressureDelta6h: Float,
        kpIndex: Float,
        tempDelta24h: Float,
        humidity: Int,
        profile: UserProfile
    ): Breakdown {
        val pressurePenalty = (abs(pressureDelta6h) * 4f).coerceIn(0f, 35f)
        val kpPenalty = ((kpIndex - 2f) * 10f).coerceIn(0f, 40f)
        val tempPenalty = ((abs(tempDelta24h) - 3f) * 3f).coerceIn(0f, 25f)
        val humidityPenalty = ((humidity - 70f) * 0.5f).coerceIn(0f, 10f)
        val personalPenalty = personalPenalty(profile, kpIndex, pressureDelta6h)
        val total = (100f - pressurePenalty - kpPenalty - tempPenalty - humidityPenalty - personalPenalty)
            .toInt().coerceIn(0, 100)
        return Breakdown(
            pressurePenalty = pressurePenalty.toInt(),
            kpPenalty = kpPenalty.toInt(),
            temperaturePenalty = tempPenalty.toInt(),
            humidityPenalty = humidityPenalty.toInt(),
            personalPenalty = personalPenalty.toInt(),
            total = total
        )
    }

    /**
     * Светофор по сочетанию факторов (правка 2.1 «Визуализация данных»).
     * Зелёный — все факторы в норме. Жёлтый — один отклонён. Красный — два и более
     * либо буря+скачок давления одновременно.
     */
    fun classifySeverity(
        pressureDelta6h: Float,
        kpIndex: Float,
        tempDelta24h: Float,
        humidity: Int
    ): Severity {
        val pressureAlert = abs(pressureDelta6h) >= 4f
        val kpAlert = kpIndex >= 4f
        val tempAlert = abs(tempDelta24h) >= 6f
        val humidityAlert = humidity >= 85
        val alerts = listOf(pressureAlert, kpAlert, tempAlert, humidityAlert).count { it }
        val stormAndPressure = (kpIndex >= 5f) && (abs(pressureDelta6h) >= 4f)
        return when {
            stormAndPressure || alerts >= 2 -> Severity.RED
            alerts == 1 -> Severity.YELLOW
            else -> Severity.GREEN
        }
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
        val multiplier = when (profile.sensitivity) {
            Sensitivity.LIGHT -> 1.0f
            Sensitivity.MODERATE -> 1.3f
            Sensitivity.STRONG -> 1.6f
        }
        return (penalty * multiplier).coerceIn(0f, 25f)
    }
}
