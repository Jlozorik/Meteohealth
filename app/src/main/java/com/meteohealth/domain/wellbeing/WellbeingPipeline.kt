package com.meteohealth.domain.wellbeing

/**
 * Вычисляет индекс самочувствия (0–100) как fold по списку стратегий Penalty.
 * Каждый Penalty тестируется изолированно; Pipeline агрегирует.
 */
class WellbeingPipeline(val penalties: List<Penalty>) {
    fun compute(input: WellbeingInput): WellbeingResult {
        val breakdown = mutableMapOf<String, Int>()
        var score = 100
        for (p in penalties) {
            val v = p.apply(input)
            breakdown[p.key] = v
            score -= v
        }
        return WellbeingResult(score.coerceIn(0, 100), breakdown.toMap())
    }

    companion object {
        fun default(): WellbeingPipeline = WellbeingPipeline(
            listOf(PressurePenalty, KpPenalty, TempPenalty, HumidityPenalty, PersonalPenalty)
        )
    }
}
