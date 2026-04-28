package com.meteohealth.data.repository

import com.meteohealth.data.local.dao.UserProfileDao
import com.meteohealth.data.local.entity.UserProfileEntity
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

    private fun UserProfileEntity.toDomain() = UserProfile(
        id = id,
        name = name,
        hasHypertension = hasHypertension,
        hasMigraines = hasMigraines,
        hasJointPain = hasJointPain,
        hasRespiratoryIssues = hasRespiratoryIssues,
        notificationsEnabled = notificationsEnabled,
        notificationThreshold = notificationThreshold,
        onboardingCompleted = onboardingCompleted,
        pressureUnit = runCatching { com.meteohealth.domain.model.PressureUnit.valueOf(pressureUnit) }
            .getOrDefault(com.meteohealth.domain.model.PressureUnit.MMHG),
        isDarkTheme = isDarkTheme
    )

    private fun UserProfile.toEntity() = UserProfileEntity(
        id = id,
        name = name,
        hasHypertension = hasHypertension,
        hasMigraines = hasMigraines,
        hasJointPain = hasJointPain,
        hasRespiratoryIssues = hasRespiratoryIssues,
        notificationsEnabled = notificationsEnabled,
        notificationThreshold = notificationThreshold,
        onboardingCompleted = onboardingCompleted,
        pressureUnit = pressureUnit.name,
        isDarkTheme = isDarkTheme
    )
}
