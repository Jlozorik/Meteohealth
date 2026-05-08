package com.meteohealth.feature.forecast

object ForecastReducer {
    fun reduce(state: ForecastState, intent: ForecastIntent): Pair<ForecastState, List<ForecastEffect>> =
        when (intent) {
            is ForecastIntent.FeedArrived -> state.copy(
                days = intent.days,
                kpSamples = intent.kpSamples,
                isLoading = false,
                error = null,
            ) to emptyList()
            is ForecastIntent.ToggleDay -> {
                val updated = state.days.mapIndexed { i, day ->
                    if (i == intent.index) day.copy(expanded = !day.expanded) else day
                }
                state.copy(days = updated) to emptyList()
            }
            is ForecastIntent.TogglePressureUnit ->
                state.copy(pressureUnit = intent.unit) to emptyList()
            is ForecastIntent.ErrorOccurred ->
                state.copy(isLoading = false, error = intent.message) to emptyList()
        }
}
