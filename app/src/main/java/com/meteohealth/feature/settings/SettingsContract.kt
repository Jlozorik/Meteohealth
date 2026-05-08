package com.meteohealth.feature.settings

import com.meteohealth.domain.model.PressureUnit
import com.meteohealth.domain.model.Profile

data class SettingsState(
    val profile: Profile = Profile(),
    val expandedSection: String? = null,
    val cityQuery: String = "",
    val showClearConfirm: Boolean = false,
    val isLoading: Boolean = true,
)

sealed interface SettingsIntent {
    data class ProfileArrived(val profile: Profile) : SettingsIntent
    data class ToggleSection(val section: String) : SettingsIntent
    data class NotifToggled(val kind: String, val enabled: Boolean) : SettingsIntent
    data class PressureUnitChanged(val unit: PressureUnit) : SettingsIntent
    data class CityQueryChanged(val query: String) : SettingsIntent
    data class CitySelected(val city: String, val lat: Double, val lon: Double) : SettingsIntent
    data class SensitivityChanged(val value: Int) : SettingsIntent
    data object RequestClearConfirm : SettingsIntent
    data object DismissClearConfirm : SettingsIntent
    data object ConfirmClear : SettingsIntent
}

sealed interface SettingsEffect {
    data object JournalCleared : SettingsEffect
}
