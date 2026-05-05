package com.meteohealth.data.repository

import com.meteohealth.data.local.dao.UserProfileDao
import com.meteohealth.data.local.entity.UserProfileEntity
import com.meteohealth.domain.model.PressureUnit
import com.meteohealth.domain.model.Sensitivity
import com.meteohealth.domain.model.UserProfile
import com.meteohealth.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserProfileRepositoryImpl(
    private val dao: UserProfileDao
) : UserProfileRepository {

    override fun observe(): Flow<UserProfile> =
        dao.observe().map { it?.toDomain() ?: UserProfile() }

    override suspend fun save(profile: UserProfile) {
        dao.upsert(profile.toEntity())
    }

    override suspend fun reset() {
        dao.deleteAll()
    }

    private fun UserProfileEntity.toDomain() = UserProfile(
        id = id,
        name = name,
        age = age,
        sensitivity = runCatching { Sensitivity.valueOf(sensitivity) }
            .getOrDefault(Sensitivity.MODERATE),
        hasHypertension = hasHypertension,
        hasMigraines = hasMigraines,
        hasJointPain = hasJointPain,
        hasRespiratoryIssues = hasRespiratoryIssues,
        notificationsEnabled = notificationsEnabled,
        notificationThreshold = notificationThreshold,
        notifyPressureJump = notifyPressureJump,
        notifyGeomagneticStorm = notifyGeomagneticStorm,
        notifyFrost = notifyFrost,
        notifyHeat = notifyHeat,
        onboardingCompleted = onboardingCompleted,
        pressureUnit = runCatching { PressureUnit.valueOf(pressureUnit) }
            .getOrDefault(PressureUnit.MMHG),
        isDarkTheme = isDarkTheme,
        cityName = cityName,
        latitude = latitude,
        longitude = longitude
    )

    private fun UserProfile.toEntity() = UserProfileEntity(
        id = id,
        name = name,
        age = age,
        sensitivity = sensitivity.name,
        hasHypertension = hasHypertension,
        hasMigraines = hasMigraines,
        hasJointPain = hasJointPain,
        hasRespiratoryIssues = hasRespiratoryIssues,
        notificationsEnabled = notificationsEnabled,
        notificationThreshold = notificationThreshold,
        notifyPressureJump = notifyPressureJump,
        notifyGeomagneticStorm = notifyGeomagneticStorm,
        notifyFrost = notifyFrost,
        notifyHeat = notifyHeat,
        onboardingCompleted = onboardingCompleted,
        pressureUnit = pressureUnit.name,
        isDarkTheme = isDarkTheme,
        cityName = cityName,
        latitude = latitude,
        longitude = longitude
    )
}
