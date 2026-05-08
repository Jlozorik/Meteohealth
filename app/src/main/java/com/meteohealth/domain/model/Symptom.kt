package com.meteohealth.domain.model

enum class Symptom {
    HEADACHE, FATIGUE, PRESSURE, DIZZINESS, NAUSEA,
    JOINT_PAIN, MOOD, INSOMNIA, HEART, OTHER;

    fun label(): String = when (this) {
        HEADACHE   -> "Голова"
        FATIGUE    -> "Усталость"
        PRESSURE   -> "Давление"
        DIZZINESS  -> "Головокружение"
        NAUSEA     -> "Тошнота"
        JOINT_PAIN -> "Суставы"
        MOOD       -> "Настроение"
        INSOMNIA   -> "Сон"
        HEART      -> "Сердце"
        OTHER      -> "Другое"
    }
}
