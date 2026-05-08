package com.meteohealth.feature.home

object HomeReducer {
    fun reduce(state: HomeState, intent: HomeIntent): Pair<HomeState, List<HomeEffect>> =
        when (intent) {
            is HomeIntent.Refresh -> state.copy(isLoading = true, error = null) to emptyList()
            is HomeIntent.FeedArrived -> state.copy(
                score = intent.score,
                riskLevel = intent.riskLevel,
                verdict = intent.verdict,
                weather = intent.weather,
                kp = intent.kp,
                pressureDelta6h = intent.pressureDelta,
                pressureUnit = intent.pressureUnit,
                recommendations = intent.recommendations,
                isLoading = false,
                error = null,
            ) to emptyList()
            is HomeIntent.ErrorOccurred -> state.copy(
                isLoading = false,
                error = intent.message,
            ) to listOf(HomeEffect.ShowRefreshError)
        }
}
