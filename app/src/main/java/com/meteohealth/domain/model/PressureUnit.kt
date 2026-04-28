package com.meteohealth.domain.model

enum class PressureUnit { HPA, MMHG }

fun Float.toDisplayPressure(unit: PressureUnit): String = when (unit) {
    PressureUnit.MMHG -> "${(this * 0.750062f).toInt()} мм рт.ст."
    PressureUnit.HPA  -> "${this.toInt()} гПа"
}
