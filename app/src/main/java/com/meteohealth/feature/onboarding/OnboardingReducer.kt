package com.meteohealth.feature.onboarding

object OnboardingReducer {
    private const val TOTAL_STEPS = 4

    fun reduce(state: OnboardingState, intent: OnboardingIntent): Pair<OnboardingState, List<OnboardingEffect>> =
        when (intent) {
            is OnboardingIntent.NameChanged -> state.copy(name = intent.value) to emptyList()
            is OnboardingIntent.AgeChanged -> state.copy(age = intent.value) to emptyList()
            is OnboardingIntent.SensitivityChanged -> state.copy(sensitivity = intent.value) to emptyList()
            is OnboardingIntent.ConditionToggled -> {
                val updated = if (intent.condition in state.conditions) {
                    state.conditions - intent.condition
                } else {
                    state.conditions + intent.condition
                }
                state.copy(conditions = updated) to emptyList()
            }
            is OnboardingIntent.CityChanged -> state.copy(city = intent.value) to emptyList()
            is OnboardingIntent.Next -> {
                if (state.step < TOTAL_STEPS - 1) {
                    state.copy(step = state.step + 1) to emptyList()
                } else {
                    state to listOf(OnboardingEffect.Finished)
                }
            }
            is OnboardingIntent.Back -> {
                if (state.step > 0) state.copy(step = state.step - 1) to emptyList()
                else state to emptyList()
            }
        }
}
