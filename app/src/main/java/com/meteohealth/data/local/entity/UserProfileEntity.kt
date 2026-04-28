package com.meteohealth.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val hasHypertension: Boolean = false,
    val hasMigraines: Boolean = false,
    val hasJointPain: Boolean = false,
    val hasRespiratoryIssues: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val notificationThreshold: Int = 60,
    val onboardingCompleted: Boolean = false,
    @ColumnInfo(defaultValue = "MMHG") val pressureUnit: String = "MMHG",
    @ColumnInfo(defaultValue = "1") val isDarkTheme: Boolean = true
)
