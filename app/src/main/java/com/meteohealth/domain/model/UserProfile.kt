package com.meteohealth.domain.model

data class UserProfile(
    val id: Int = 1,
    val name: String = "",
    val hasHypertension: Boolean = false,
    val hasMigraines: Boolean = false,
    val hasJointPain: Boolean = false,
    val hasRespiratoryIssues: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val notificationThreshold: Int = 60,
    val onboardingCompleted: Boolean = false
)
