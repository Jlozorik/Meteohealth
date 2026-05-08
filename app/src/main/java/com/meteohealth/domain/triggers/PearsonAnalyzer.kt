package com.meteohealth.domain.triggers

import kotlin.math.sqrt

data class TriggerResult(
    val factor: String,
    val r: Double,
    val sampleCount: Int,
)

enum class CorrelationStrength { NONE, WEAK, MODERATE, STRONG }

object PearsonAnalyzer {

    fun analyze(
        levels: List<Double>,
        factorValues: List<Double>,
        factorName: String,
    ): TriggerResult {
        require(levels.size == factorValues.size) { "Lists must have the same size" }
        val n = levels.size
        if (n < 2) return TriggerResult(factorName, 0.0, n)

        val r = pearson(levels, factorValues)
        return TriggerResult(factorName, r, n)
    }

    fun strength(r: Double): CorrelationStrength {
        val abs = kotlin.math.abs(r)
        return when {
            abs >= 0.5 -> CorrelationStrength.STRONG
            abs >= 0.3 -> CorrelationStrength.MODERATE
            abs >= 0.1 -> CorrelationStrength.WEAK
            else       -> CorrelationStrength.NONE
        }
    }

    private fun pearson(x: List<Double>, y: List<Double>): Double {
        val n = x.size.toDouble()
        val meanX = x.sum() / n
        val meanY = y.sum() / n
        var num = 0.0
        var denX = 0.0
        var denY = 0.0
        for (i in x.indices) {
            val dx = x[i] - meanX
            val dy = y[i] - meanY
            num += dx * dy
            denX += dx * dx
            denY += dy * dy
        }
        val den = sqrt(denX * denY)
        return if (den == 0.0) 0.0 else num / den
    }
}
