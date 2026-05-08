package com.meteohealth.domain.wellbeing

import kotlin.math.abs

interface Penalty {
    val key: String
    fun apply(input: WellbeingInput): Int
}

/** clamp(|ΔP_6h| × 4, 0, 30) */
object PressurePenalty : Penalty {
    override val key = "pressure"
    override fun apply(input: WellbeingInput): Int =
        (abs(input.pressureDelta6h) * 4).toInt().coerceIn(0, 30)
}

/** clamp((Kp − 3) × 8, 0, 30) */
object KpPenalty : Penalty {
    override val key = "kp"
    override fun apply(input: WellbeingInput): Int =
        ((input.kpIndex - 3.0) * 8).toInt().coerceIn(0, 30)
}

/** clamp((|ΔT_24h| − 5) × 2, 0, 20) */
object TempPenalty : Penalty {
    override val key = "temp"
    override fun apply(input: WellbeingInput): Int =
        ((abs(input.tempDelta24h) - 5.0) * 2).toInt().coerceIn(0, 20)
}

/** clamp((humidity − 70) × 0.5, 0, 10) */
object HumidityPenalty : Penalty {
    override val key = "humidity"
    override fun apply(input: WellbeingInput): Int =
        ((input.humidity - 70.0) * 0.5).toInt().coerceIn(0, 10)
}

/**
 * Личный штраф: sensitivity (1–5) масштабирует сумму остальных факторов.
 * При sensitivity=3 — нейтраль (0 доп. штрафа).
 * Хронические условия (migraine, hypertension, …) добавляют +2 за каждое.
 */
object PersonalPenalty : Penalty {
    override val key = "personal"
    override fun apply(input: WellbeingInput): Int {
        val sensitivityBonus = (input.profile.sensitivity - 3) * 2
        val conditionBonus = input.profile.healthConditions.size * 2
        return (sensitivityBonus + conditionBonus).coerceIn(0, 10)
    }
}
