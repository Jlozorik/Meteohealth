package com.meteohealth.domain.model

enum class Sensitivity { LIGHT, MODERATE, STRONG }

data class UserProfile(
    val id: Int = 1,
    val name: String = "",
    val age: Int? = null,
    val sensitivity: Sensitivity = Sensitivity.MODERATE,
    val hasHypertension: Boolean = false,
    val hasMigraines: Boolean = false,
    val hasJointPain: Boolean = false,
    val hasRespiratoryIssues: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val notificationThreshold: Int = 60,
    val notifyPressureJump: Boolean = true,
    val notifyGeomagneticStorm: Boolean = true,
    val notifyFrost: Boolean = true,
    val notifyHeat: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val pressureUnit: PressureUnit = PressureUnit.MMHG,
    val isDarkTheme: Boolean = true,
    val cityName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
