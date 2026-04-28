package com.meteohealth.domain

import com.meteohealth.domain.model.DiaryEntry
import com.meteohealth.domain.model.WellbeingLevel
import kotlin.math.abs

/**
 * Линейная регрессия (МНК) по записям дневника.
 * Признаки: давление (гПа), температура (°C), Kp-индекс → персональный индекс самочувствия (0–100).
 * Обучение: normal equations w = (XᵀX)⁻¹ Xᵀy.
 */
class WellbeingPredictor {

    private var weights: DoubleArray? = null  // [bias, pressure, temp, kp]

    val isTrained: Boolean get() = weights != null
    var sampleCount: Int = 0
        private set

    companion object {
        const val MIN_SAMPLES = 10
    }

    fun train(entries: List<DiaryEntry>) {
        val valid = entries.filter {
            it.pressureHpa != null && it.temperatureCelsius != null && it.kpIndex != null
        }
        sampleCount = valid.size
        if (valid.size < MIN_SAMPLES) { weights = null; return }

        val n = valid.size
        val X = Array(n) { i ->
            val e = valid[i]
            doubleArrayOf(1.0, e.pressureHpa!!.toDouble(), e.temperatureCelsius!!.toDouble(), e.kpIndex!!.toDouble())
        }
        val y = DoubleArray(n) { i -> valid[i].wellbeingLevel.toScore().toDouble() }

        val Xt = transpose(X)
        val XtX = multiply(Xt, X)
        val Xty = multiplyMV(Xt, y)
        weights = invertMatrix(XtX)?.let { multiplyMV(it, Xty) }
    }

    /** Возвращает персональный индекс 0–100 или -1 если модель не обучена. */
    fun predict(pressureHpa: Float, temperatureCelsius: Float, kpIndex: Float = 0f): Int {
        val w = weights ?: return -1
        val score = w[0] + w[1] * pressureHpa + w[2] * temperatureCelsius + w[3] * kpIndex
        return score.toInt().coerceIn(0, 100)
    }

    private fun WellbeingLevel.toScore(): Int = when (this) {
        WellbeingLevel.GREAT    -> 90
        WellbeingLevel.GOOD     -> 72
        WellbeingLevel.FAIR     -> 50
        WellbeingLevel.POOR     -> 28
        WellbeingLevel.TERRIBLE -> 10
    }

    private fun transpose(m: Array<DoubleArray>): Array<DoubleArray> {
        val rows = m.size; val cols = m[0].size
        return Array(cols) { i -> DoubleArray(rows) { j -> m[j][i] } }
    }

    private fun multiply(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        val n = a.size; val p = b[0].size; val k = b.size
        return Array(n) { i -> DoubleArray(p) { j -> (0 until k).sumOf { l -> a[i][l] * b[l][j] } } }
    }

    private fun multiplyMV(a: Array<DoubleArray>, v: DoubleArray): DoubleArray =
        DoubleArray(a.size) { i -> a[i].indices.sumOf { j -> a[i][j] * v[j] } }

    private fun invertMatrix(m: Array<DoubleArray>): Array<DoubleArray>? {
        val n = m.size
        val aug = Array(n) { i ->
            DoubleArray(2 * n) { j -> if (j < n) m[i][j] else if (j - n == i) 1.0 else 0.0 }
        }
        for (col in 0 until n) {
            val pivotRow = (col until n).maxByOrNull { abs(aug[it][col]) } ?: return null
            aug[col] = aug[pivotRow].also { aug[pivotRow] = aug[col] }
            val pivot = aug[col][col]
            if (abs(pivot) < 1e-10) return null
            for (j in 0 until 2 * n) aug[col][j] /= pivot
            for (row in 0 until n) {
                if (row == col) continue
                val factor = aug[row][col]
                for (j in 0 until 2 * n) aug[row][j] -= factor * aug[col][j]
            }
        }
        return Array(n) { i -> DoubleArray(n) { j -> aug[i][j + n] } }
    }
}
