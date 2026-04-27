package com.meteohealth.domain

import com.meteohealth.domain.model.DiaryEntry
import com.meteohealth.domain.model.WellbeingLevel
import kotlin.math.pow
import kotlin.math.sqrt

object TriggerAnalyzer {

    private const val MIN_TOTAL_ENTRIES = 14
    private const val MIN_FACTOR_ENTRIES = 5

    enum class Factor(val label: String) {
        PRESSURE("Давление"),
        TEMPERATURE("Температура"),
        KP_INDEX("Геомагн. активность")
    }

    data class TriggerResult(
        val factor: Factor,
        val correlation: Float,
        val entryCount: Int
    )

    fun analyze(entries: List<DiaryEntry>): List<TriggerResult> {
        if (entries.size < MIN_TOTAL_ENTRIES) return emptyList()

        return Factor.entries.mapNotNull { factor ->
            val pairs = entries.mapNotNull { entry ->
                val x = factorValue(factor, entry) ?: return@mapNotNull null
                entry.wellbeingLevel.toScore().toDouble() to x.toDouble()
            }
            if (pairs.size < MIN_FACTOR_ENTRIES) return@mapNotNull null
            val r = pearson(pairs.map { it.first }, pairs.map { it.second })
            TriggerResult(factor, r.toFloat(), pairs.size)
        }
    }

    private fun factorValue(factor: Factor, entry: DiaryEntry): Float? = when (factor) {
        Factor.PRESSURE -> entry.pressureHpa
        Factor.TEMPERATURE -> entry.temperatureCelsius
        Factor.KP_INDEX -> entry.kpIndex
    }

    private fun WellbeingLevel.toScore(): Int = when (this) {
        WellbeingLevel.GREAT -> 5
        WellbeingLevel.GOOD -> 4
        WellbeingLevel.FAIR -> 3
        WellbeingLevel.POOR -> 2
        WellbeingLevel.TERRIBLE -> 1
    }

    private fun pearson(xs: List<Double>, ys: List<Double>): Double {
        val n = xs.size
        if (n < 2) return 0.0
        val meanX = xs.average()
        val meanY = ys.average()
        val num = xs.zip(ys).sumOf { (x, y) -> (x - meanX) * (y - meanY) }
        val denX = sqrt(xs.sumOf { (it - meanX).pow(2) })
        val denY = sqrt(ys.sumOf { (it - meanY).pow(2) })
        return if (denX == 0.0 || denY == 0.0) 0.0 else num / (denX * denY)
    }
}
