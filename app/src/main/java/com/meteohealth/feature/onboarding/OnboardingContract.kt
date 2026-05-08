package com.meteohealth.feature.onboarding

data class OnboardingState(
    val step: Int = 0,
    val name: String = "",
    val age: String = "",
    val sensitivity: Int = 3,
    val conditions: Set<String> = emptySet(),
    val city: String = "",
)

sealed interface OnboardingIntent {
    data class NameChanged(val value: String) : OnboardingIntent
    data class AgeChanged(val value: String) : OnboardingIntent
    data class SensitivityChanged(val value: Int) : OnboardingIntent
    data class ConditionToggled(val condition: String) : OnboardingIntent
    data class CityChanged(val value: String) : OnboardingIntent
    data object Next : OnboardingIntent
    data object Back : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object Finished : OnboardingEffect
}
