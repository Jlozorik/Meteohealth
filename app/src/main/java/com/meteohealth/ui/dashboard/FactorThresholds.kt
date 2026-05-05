package com.meteohealth.ui.dashboard

import androidx.compose.ui.graphics.Color
import com.meteohealth.domain.WellbeingCalculator.Severity
import com.meteohealth.ui.theme.SeverityGreen
import com.meteohealth.ui.theme.SeverityRed
import com.meteohealth.ui.theme.SeverityYellow
import kotlin.math.abs

/**
 * Пороги «зелёный/жёлтый/красный» для каждой метеометрики на дашборде.
 * Используются для подсветки иконок факторов (правка 2.4 Pravka2).
 */
object FactorThresholds {
    fun pressureDelta(deltaHpa: Float): Severity {
        val v = abs(deltaHpa)
        return when {
            v >= 6f -> Severity.RED
            v >= 3f -> Severity.YELLOW
            else -> Severity.GREEN
        }
    }

    fun temperatureDelta(deltaC: Float): Severity {
        val v = abs(deltaC)
        return when {
            v >= 10f -> Severity.RED
            v >= 6f -> Severity.YELLOW
            else -> Severity.GREEN
        }
    }

    fun humidity(percent: Int): Severity = when {
        percent >= 90 || percent <= 25 -> Severity.RED
        percent >= 80 || percent <= 35 -> Severity.YELLOW
        else -> Severity.GREEN
    }

    fun wind(speedMs: Float): Severity = when {
        speedMs >= 14f -> Severity.RED
        speedMs >= 8f -> Severity.YELLOW
        else -> Severity.GREEN
    }

    fun kp(kpIndex: Float): Severity = when {
        kpIndex >= 6f -> Severity.RED
        kpIndex >= 4f -> Severity.YELLOW
        else -> Severity.GREEN
    }
}

fun Severity.toColor(): Color = when (this) {
    Severity.GREEN -> SeverityGreen
    Severity.YELLOW -> SeverityYellow
    Severity.RED -> SeverityRed
}

fun Severity.toLabel(): String = when (this) {
    Severity.GREEN -> "Норма"
    Severity.YELLOW -> "Внимание"
    Severity.RED -> "Опасно"
}
