package com.meteohealth.feature.settings

object SettingsReducer {
    fun reduce(state: SettingsState, intent: SettingsIntent): Pair<SettingsState, List<SettingsEffect>> =
        when (intent) {
            is SettingsIntent.ProfileArrived -> state.copy(profile = intent.profile, isLoading = false) to emptyList()
            is SettingsIntent.ToggleSection -> state.copy(
                expandedSection = if (state.expandedSection == intent.section) null else intent.section
            ) to emptyList()
            is SettingsIntent.NotifToggled -> {
                val updated = state.profile.notificationPrefs.toMutableMap().also {
                    it[intent.kind] = intent.enabled
                }
                state.copy(profile = state.profile.copy(notificationPrefs = updated)) to emptyList()
            }
            is SettingsIntent.PressureUnitChanged ->
                state.copy(profile = state.profile.copy(pressureUnit = intent.unit)) to emptyList()
            is SettingsIntent.CityQueryChanged -> state.copy(cityQuery = intent.query) to emptyList()
            is SettingsIntent.CitySelected -> state.copy(
                profile = state.profile.copy(city = intent.city, lat = intent.lat, lon = intent.lon),
                cityQuery = "",
                expandedSection = null,
            ) to emptyList()
            is SettingsIntent.SensitivityChanged ->
                state.copy(profile = state.profile.copy(sensitivity = intent.value)) to emptyList()
            is SettingsIntent.RequestClearConfirm -> state.copy(showClearConfirm = true) to emptyList()
            is SettingsIntent.DismissClearConfirm -> state.copy(showClearConfirm = false) to emptyList()
            is SettingsIntent.ConfirmClear -> state.copy(showClearConfirm = false) to listOf(SettingsEffect.JournalCleared)
        }
}
